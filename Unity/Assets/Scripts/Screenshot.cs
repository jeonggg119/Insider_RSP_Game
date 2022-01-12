using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using System.Net;
using System.Net.Sockets;
using System.IO;
using System;
using System.Text;
using System.Threading;
using System.Globalization;
using System.Runtime.InteropServices;


//public struct ToServerPacket
//{
//    public string player1_class_name;
//    public string player2_class_name;
//    public string result;
//}

//public struct ToClientPacket
//{
//    public string width;
//    public string height;
//    public string channels;
//    public byte[] jpeg_data;
//}


public class Screenshot : MonoBehaviour
{
    private Socket m_Server, m_Client;
    //private Thread thread;
    public int m_Port = 50000;
    //public ToClientPacket m_SendPacket = new ToClientPacket();
    //public ToServerPacket m_ReceivePacket = new ToServerPacket();
    private EndPoint m_RemoteEndPoint;

    Player1 player1_script;
    Player2 player2_script;

    public string player1_class_name, player2_class_name;
    public string result;

    void Start()
    {
        InitServer();
        player1_script = FindObjectOfType<Player1>();
        player2_script = FindObjectOfType<Player2>();

        //thread = new Thread(Run);
        //thread.Start();

        //class_name1 = "1Rock";
        //class_name2 = "3Scissors";
        //result_name1 = "WIN";
        //result_name2 = "LOSE";
        //player1_script.ChangeResults();
        //player2_script.ChangeResults();
        //Capture("1");

        //class_name1 = "15Gun";
        //class_name2 = "13Devil";
        //result_name1 = "WIN";
        //result_name2 = "LOSE";
        //player1_script.ChangeResults();
        //player2_script.ChangeResults();
        //Capture("2");
    }

    void Update()
    {
        m_Client = m_Server.Accept();
        bool isReceived = Receive();
        if (isReceived)
            Send();
        CloseClient();
    }

    void OnApplicationQuit()
    {
        CloseServer();
    }

    void InitServer()
    {
        // SendPacket에 배열이 있으면 선언 해 주어야 함.
        //m_SendPacket.m_IntArray = new int[2];
        m_Server = new Socket(AddressFamily.InterNetwork, SocketType.Stream, ProtocolType.Tcp);
        m_RemoteEndPoint = new IPEndPoint(IPAddress.Any, m_Port);
        m_Server.Bind(m_RemoteEndPoint);
        m_Server.Listen(10); // client 접속을 기다림.
        // client가 하나만 사용.
    }

    bool Receive()
    {
        int receive = 0;
        int player1_strlen, player2_strlen, result_strlen;
        byte[] packet = new byte[1];

        try
        {

            receive = m_Client.Receive(packet);
            player1_strlen = Convert.ToInt32(Encoding.Default.GetString(packet));
            byte[] player1_bytes = new byte[player1_strlen];
            receive += m_Client.Receive(player1_bytes);
            player1_class_name = Encoding.Default.GetString(player1_bytes);
            Debug.Log(player1_class_name);

            receive = m_Client.Receive(packet);
            player2_strlen = Convert.ToInt32(Encoding.Default.GetString(packet));
            byte[] player2_bytes = new byte[player2_strlen];
            receive += m_Client.Receive(player2_bytes);
            player2_class_name = Encoding.Default.GetString(player2_bytes);
            Debug.Log(player2_class_name);

            receive = m_Client.Receive(packet);
            result_strlen = Convert.ToInt32(Encoding.Default.GetString(packet));
            byte[] result_bytes = new byte[result_strlen];
            receive += m_Client.Receive(result_bytes);
            result = Encoding.Default.GetString(result_bytes);
            Debug.Log(result);
        }
        catch (Exception ex)
        {
            //Debug.Log(ex.ToString());
            return false;
        }

        //m_ReceivePacket = ByteArrayToStruct<ToServerPacket>(packet);

        if (receive > 0)
        {
            DoReceivePacket(); // 받은 값 처리
        }
        return true;
    }

    void DoReceivePacket()
    {
        //player1_class_name = m_ReceivePacket.player1_class_name;
        //player2_class_name = m_ReceivePacket.player2_class_name;
        //result = m_ReceivePacket.result;
        //Debug.Log(player1_class_name);
        //Debug.Log(player2_class_name);
        //Debug.Log(result);
        player1_script.ChangeResults();
        player2_script.ChangeResults();
    }

    void CloseClient()
    {
        if (m_Client != null)
        {
            m_Client.Close();
            m_Client = null;
        }
    }

    void Send()
    {
        try
        {
            //SetSendPacket();
            //byte[] sendPacket = StructToByteArray(m_SendPacket);
            byte[] jpgBytes = Capture();
            Debug.Log(jpgBytes.Length.ToString());
            byte[] intBytes = BitConverter.GetBytes(jpgBytes.Length);
            //Debug.Log(Encoding.Default.GetString(intBytes));
            byte[] sendPacket = Concat(intBytes, jpgBytes);
            m_Client.Send(sendPacket, 0, sendPacket.Length, SocketFlags.None);
        }

        catch (Exception ex)
        {
            Debug.Log(ex.ToString());
            return;
        }
    }

    //void SetSendPacket()
    //{
    //    m_SendPacket.m_BoolVariable = true;
    //    m_SendPacket.m_IntVariable = 13;
    //    m_SendPacket.m_IntArray[0] = 7;
    //    m_SendPacket.m_IntArray[1] = 47;
    //    m_SendPacket.m_FloatlVariable = 2020;
    //    m_SendPacket.m_StringlVariable = "Coder Zero";
    //}

    void CloseServer()
    {
        if (m_Client != null)
        {
            m_Client.Close();
            m_Client = null;
        }

        if (m_Server != null)
        {
            m_Server.Close();
            m_Server = null;
        }
    }

    byte[] StructToByteArray(object obj)
    {
        int size = Marshal.SizeOf(obj);
        byte[] arr = new byte[size];
        IntPtr ptr = Marshal.AllocHGlobal(size);

        Marshal.StructureToPtr(obj, ptr, true);
        Marshal.Copy(ptr, arr, 0, size);
        Marshal.FreeHGlobal(ptr);
        return arr;
    }

    T ByteArrayToStruct<T>(byte[] buffer) where T : struct
    {
        int size = Marshal.SizeOf(typeof(T));
        if (size > buffer.Length)
        {
            throw new Exception();
        }

        IntPtr ptr = Marshal.AllocHGlobal(size);
        Marshal.Copy(buffer, 0, ptr, size);
        T obj = (T)Marshal.PtrToStructure(ptr, typeof(T));
        Marshal.FreeHGlobal(ptr);
        return obj;
    }

    static byte[] Concat(byte[] a, byte[] b)
    {
        byte[] output = new byte[a.Length + b.Length];
        for (int i = 0; i < a.Length; i++)
            output[i] = a[i];
        for (int j = 0; j < b.Length; j++)
            output[a.Length + j] = b[j];
        return output;
    }

    // Screenshot 캡쳐 함수
    byte[] Capture()
    {
        Camera camera = Camera.main;

        var width = Screen.width;
        var height = Screen.height;

        var renderTexture = new RenderTexture(width, height, (int)camera.depth);
        camera.targetTexture = renderTexture;
        camera.Render();
        RenderTexture.active = renderTexture;

        var imageTexture = new Texture2D(width, height, TextureFormat.RGB24, false);
        imageTexture.ReadPixels(new Rect(0, 0, width, height), 0, 0);
        byte[] jpgData = imageTexture.EncodeToJPG();
        //WriteTexture(imageTexture);

        camera.targetTexture = null;
        RenderTexture.active = null;
        Destroy(renderTexture);
        return jpgData;
    }


    // Texture 저장 함수
    private void WriteTexture(Texture2D texture)
    {
        byte[] jpgData = texture.EncodeToJPG();

        var now = System.DateTime.Now;
        string fileName = "ScreenShot" + ".jpg";
        string folderPath = Application.dataPath + "/" + "ScreenShot";
        string filePath = folderPath + '/' + fileName;

        if (!Directory.Exists(folderPath))
        {
            Directory.CreateDirectory(folderPath);
        }

        File.WriteAllBytes(filePath, jpgData);
    }


}
