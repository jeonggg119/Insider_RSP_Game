using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class Player2 : MonoBehaviour
{
    public GameObject prefab;
    public GameObject result;
    public GameObject prefabInstance;
    public GameObject resultInstance;
    public Screenshot main_script;

    void Start()
    {
        main_script = FindObjectOfType<Screenshot>();
        prefabInstance = null;
        resultInstance = null;
    }

    void Update()
    {
        
    }

    public void ChangeResults()
    {
        if (prefabInstance != null)
            DestroyImmediate(prefabInstance, true);
        //Destroy(prefabInstance);
        //if (resultInstance != null)
        //    DestroyImmediate(resultInstance, true);
        //Destroy(resultInstance);
        prefab = Resources.Load<GameObject>("Prefabs/" + main_script.player2_class_name);
        Vector3 rotateVector1 = new Vector3(-70, -100, 90);
        prefabInstance = (GameObject)Instantiate(prefab, new Vector3(25, 5, 0), Quaternion.Euler(rotateVector1));

        //result = Resources.Load<GameObject>("Results/" + main_script.result_name2);
        //Vector3 rotateVector2 = new Vector3(0, -180, 0);
        //resultInstance = (GameObject)Instantiate(result, new Vector3(25, 25, 0), Quaternion.Euler(rotateVector2));
    }
}
