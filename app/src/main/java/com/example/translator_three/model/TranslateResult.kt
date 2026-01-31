package com.example.translator_three.model

//这是一个kotlin数据类，专门用于映射（解析）百度翻译API返回的JSON格式响应。当Retrofit网络库接收到API响应后
//会自动将JSON转换成这个数据类的实例，方便代码中直接访问翻译结果

data class TranslateResult(
    val from: String,  //源语言
    val to: String,  //目标语言
    val trans_result: List<TransResult>  //翻译结果列表（具体的对象在TransResult类）
)
