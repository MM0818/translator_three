package com.example.translator_three.model;

import java.util.List;

public class TranslationResponse {
    private String error_code;
    private String error_msg;
    private List<TranslationResult> trans_result;

    // Getter和Setter
    //错误码
    public String getError_code() { return error_code; }
    public void setError_code(String error_code) { this.error_code = error_code; }

    //错误
    public String getError_msg() { return error_msg; }
    public void setError_msg(String error_msg) { this.error_msg = error_msg; }

    //翻译结果列表
    public List<TranslationResult> getTrans_result() { return trans_result; }
    public void setTrans_result(List<TranslationResult> trans_result) { this.trans_result = trans_result; }
    /*
    以上都是对于百度翻译API返回格式来写的，如：
    {
        "error_code": "52000",
        "error_msg": "SUCCESS",
        "trans_result": [
            {
                "src": "Hello world",
                "dst": "你好世界"
            }
        ]
    }
    */

    public static class TranslationResult {
        private String src;
        private String dst;

        public String getSrc() { return src; }
        public void setSrc(String src) { this.src = src; }

        public String getDst() { return dst; }
        public void setDst(String dst) { this.dst = dst; }
    }
}
