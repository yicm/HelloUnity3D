using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class Ball : MonoBehaviour
{
    //添加：游戏对象跑出画面外时被调用的方法
    void OnBecameInvisible()
    {
        Destroy(this.gameObject);  //删除游戏对象
    }

    // Start is called before the first frame update
    void Start()
    {
        this.GetComponent<Rigidbody>().velocity = new Vector3(-8.0f, 8.0f, 0.0f); //设置向左上方的速度
    }

    // Update is called once per frame
    void Update()
    {
        
    }
}
