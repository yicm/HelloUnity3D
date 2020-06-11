using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class Player : MonoBehaviour
{
    protected float jump_speed = 5.0f;

    // Start is called before the first frame update
    void Start()
    {
        
    }

    // Update is called once per frame
    void Update()
    {
        if (Input.GetMouseButtonDown(0))    //点击鼠标左键触发
        {
            this.GetComponent<Rigidbody>().velocity = Vector3.up * this.jump_speed;  //设定向上速度
        }
    }

    void Jump()
    {
        this.GetComponent<Rigidbody>().velocity = Vector3.up * this.jump_speed;
    }
}
