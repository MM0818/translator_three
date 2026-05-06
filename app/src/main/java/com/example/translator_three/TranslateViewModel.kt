package com.example.translator_three

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.translator_three.repository.TranslationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TranslateViewModel(application: Application) : AndroidViewModel(application) {

    // 原来的仓库，直接用！
    private val translationRepository = TranslationRepository(application)

    //原来用的LiveData
//    // 翻译结果（对外只读）
//    private val _translateResult = MutableLiveData<String?>()
//    val translateResult: LiveData<String?> = _translateResult

    //Flow（升级后的LiveData）
    private val _translateFlow = MutableStateFlow<String?>(null)
    val translateFlow: StateFlow<String?> = _translateFlow.asStateFlow()

    // 加载状态
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // 错误信息
    private val _errorMsg = MutableStateFlow<String?>(null)
    val errorMsg: StateFlow<String?> = _errorMsg.asStateFlow()

    /**
     * 文本翻译（ TextTActivity 用的）
     */

    fun translateText(word: String, fromLang: String, toLang: String) {
        if (word.isBlank()) {
            _errorMsg.value = "请输入要翻译的内容"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMsg.value = null

            val result = withContext(Dispatchers.IO) {
                translationRepository.translate(word, fromLang, toLang)
            }

            _isLoading.value = false

            if (result != null) {
                _translateFlow.value = result
            } else {
                _errorMsg.value = "翻译失败，请稍后再试"
            }
        }
    }

    /**
     * 语音翻译（ VoiceTActivity 用的）
     */
    fun translateVoice(text: String) {
        viewModelScope.launch {
            val result = translationRepository.translate(text)
            _translateFlow.value = result
        }
    }

    override fun onCleared() {
        super.onCleared()
        translationRepository.cancelAllCoroutines()
    }
}