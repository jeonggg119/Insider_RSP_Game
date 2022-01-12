import io
import os
import random
import socket
import struct

import cv2
import numpy as np
from flask import Flask, request, send_file

from model import detect

UPLOAD_FOLDER = 'static/images'
MODEL_PATH = 'MPMLP9_lefthand_5.pt'

app = Flask(__name__, template_folder='templates')
app.secret_key = 'QWFsaidk1238v@!3129sakd'
app.config['UPLOAD_FOLDER'] = UPLOAD_FOLDER

UNITY_HOST = '127.0.0.1'
UNITY_PORT = 50000

player_list = []
matchups = []


def recv_msg(sock):
    # Read message length and unpack it into an integer
    raw_msglen = recvall(sock, 4)
    if not raw_msglen:
        return None
    msglen = struct.unpack('<I', raw_msglen)[0]
    # Read the message data
    return recvall(sock, msglen)


def recvall(sock, n):
    # Helper function to recv n bytes or return None if EOF is hit
    data = bytearray()
    while len(data) < n:
        packet = sock.recv(n - len(data))
        if not packet:
            return None
        data.extend(packet)
    return data



def get_matchup(player_id):
    for matchup in matchups:
        for player_info in matchup:
            if player_id == player_info['id']:
                return matchup
    return None


def remove_matchup(player_id):
    matchup_idx = None
    for idx, matchup in enumerate(matchups):
        for player_info in matchup:
            if player_id == player_info['id']:
                matchup_idx = idx
    if matchup_idx is not None:
        matchups.pop(matchup_idx)


def get_winner(player_class, opponent_class):
    p_idx = int(player_class[:2])
    o_idx = int(opponent_class[:2])
    win_indices = [(p_idx + n) % 9 + 1 for n in range(4)]
    if o_idx == p_idx:
        return 'TIE'
    elif o_idx in win_indices:
        return 'WIN'
    else:
        return 'LOSE'


@app.route('/login', methods=['GET'])
def login():
    player_id = request.remote_addr
    if player_id is not None:
        player_list.append(player_id)
    return '당신의 아이디는 %s 입니다.' % player_id


@app.route('/match', methods=['GET'])
def match():
    player_id = request.remote_addr
    if player_id not in player_list:
        player_list.append(player_id)
    matchup = get_matchup(player_id)
    if matchup is None:
        if len(player_list) < 2:
            return '상대방이 존재하지 않습니다. 잠시 후 다시 시도해주세요.'
        player_list.remove(player_id)
        opponent_id = player_list.pop(random.randrange(len(player_list)))
        matchups.append(({'id': player_id}, {'id': opponent_id}))
    else:
        opponent_id = matchup[0]['id'] if matchup[1]['id'] == player_id else matchup[1]['id']

    return '당신의 상대방은 %s 입니다.' % opponent_id


@app.route('/upload', methods=['POST'])
def upload():
    player_id = request.remote_addr
    matchup = get_matchup(player_id)
    if matchup is None:
        return '상대방이 존재하지 않습니다. 업로드 전 매칭부터 진행해주십시오.'
    player_info = matchup[0] if matchup[0]['id'] == player_id else matchup[1]

    file = request.files['filename']
    binary = np.fromstring(file.read(), np.uint8)
    image = cv2.imdecode(binary, cv2.IMREAD_COLOR)
    image_path = os.path.join(app.config['UPLOAD_FOLDER'], '%s.jpg' % player_id)
    player_info['image'] = image
    player_info['image_path'] = image_path
    cv2.imwrite(image_path, image)

    player_info = detect(player_info, MODEL_PATH)
    if player_info['class'] is None:
        return '사진에 손이 잡히지 않았습니다. 사진을 다시 찍어서 업로드해주세요.'
    preds_path = os.path.join(app.config['UPLOAD_FOLDER'], '%s_preds.jpg' % player_id)
    player_info['predictions_path'] = preds_path
    cv2.imwrite(preds_path, player_info['predictions'])

    return '업로드가 완료되었습니다!'


@app.route('/proceed', methods=['GET'])
def proceed():
    player_id = request.remote_addr
    matchup = get_matchup(player_id)
    if matchup is None:
        return '상대방이 존재하지 않습니다. 결과 확인 전 매칭부터 진행해주십시오.'
    player_info = matchup[0] if matchup[0]['id'] == player_id else matchup[1]
    opponent_info = matchup[0] if matchup[1]['id'] == player_id else matchup[1]
    if 'image' not in player_info:
        return '당신의 이미지가 업로드 되지 않았습니다. 어떤 동작을 낼지 이미지로 업드로드해주십시오.'
    if 'image' not in opponent_info:
        return '상대방의 이미지가 업로드 되지 않았습니다. 상대방이 동작을 낼 때까지 기다려주십시오.'
    return 'proceed'


@app.route('/result', methods=['GET'])
def result():
    player_id = request.remote_addr
    matchup = get_matchup(player_id)
    player_info = matchup[0] if matchup[0]['id'] == player_id else matchup[1]
    opponent_info = matchup[0] if matchup[1]['id'] == player_id else matchup[1]

    sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    sock.connect((UNITY_HOST, UNITY_PORT))
    send_packet = bytearray()
    match_result = get_winner(player_info['class'], opponent_info['class'])
    values = [player_info['class'], opponent_info['class'], match_result]
    for value in values:
        send_packet.extend(bytes(str(len(value)), encoding='UTF-8'))
        send_packet.extend(bytes(value, encoding='UTF-8'))

    sock.sendall(send_packet)
    image_bytes = recv_msg(sock)
    sock.close()
    encoded_image = np.frombuffer(image_bytes, dtype=np.uint8)
    decoded_image = cv2.imdecode(encoded_image, cv2.IMREAD_COLOR)
    result_path = os.path.join(app.config['UPLOAD_FOLDER'], '%s_result.jpg' % player_id)
    cv2.imwrite(result_path, decoded_image)
    return send_file(result_path, mimetype='image/jpg')


# @app.route('/runaway', methods=['GET'])
# def runaway():
#     player_info['checked'] = True
#     if 'checked' in matchup[0] and 'checked' in matchup[1]:
#         player_list.append(player_info['id'])
#         player_list.append(opponent_info['id'])
#         # matchups.remove(matchup)
#         remove_matchup(player_info['id'])
#
#
# @app.route('/nextgame', methods=['GET'])
# def nextgame():
#     player_info['checked'] = True
#     if 'checked' in matchup[0] and 'checked' in matchup[1]:
#         player_list.append(player_info['id'])
#         player_list.append(opponent_info['id'])
#         # matchups.remove(matchup)
#         remove_matchup(player_info['id'])


if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5050)
