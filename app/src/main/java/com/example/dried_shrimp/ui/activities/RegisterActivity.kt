package com.example.dried_shrimp.ui.activities

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.dried_shrimp.R
import com.example.dried_shrimp.databinding.ActivityRegisterBinding
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.Firebase
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.database
import java.net.URLEncoder

class RegisterActivity : AppCompatActivity() {
    lateinit var binding : ActivityRegisterBinding
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var callbackManager: CallbackManager
    private val GOOGLE_SIGN_IN = 9001
    private val user_datas: MutableList<Map<String, Map<String, Any>>> = mutableListOf()
    val database = Firebase.database
    val myRef = database.getReference("message")



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // 上方 header 避免與狀態列重疊
        ViewCompat.setOnApplyWindowInsetsListener(binding.registerHeader) { view, insets ->
            val statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            view.setPadding(view.paddingLeft, statusBarHeight, view.paddingRight, view.paddingBottom)
            insets
        }
        // ⭐ 底部指紋認證區避免被導航列蓋住
        ViewCompat.setOnApplyWindowInsetsListener(binding.linearLayout2) { view, insets ->
            val bottomInset = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
            view.setPadding(view.paddingLeft, view.paddingTop, view.paddingRight, bottomInset)
            insets
        }
        initialization()
        setlisteners()
        Login_page()
        back()

    }
    fun Account_Registration() {
        val account = binding.registerEtaccount.text.toString().trim()
        val password = binding.registerEtpasswword.text.toString().trim()
        val phone = binding.registerEtphone.text.toString().trim()

        // 基本欄位判斷
        if (account.isEmpty() || password.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "請輸入所有欄位", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.length < 4) {
            Toast.makeText(this, "密碼至少 4 碼", Toast.LENGTH_SHORT).show()
            return
        }

        // 🔥 送到 Firebase 建立帳號
        registerUser(account, password, phone)
    }


    private fun registerUser(account: String, password: String, phone: String) {
        FirebaseAuth.getInstance()
            .createUserWithEmailAndPassword(account, password)
            .addOnCompleteListener { task ->

                if (task.isSuccessful) {
                    val uid = task.result.user!!.uid
                    saveUserProfile(uid, account, phone)
                } else {
                    Toast.makeText(this, "註冊失敗：${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun saveUserProfile(uid: String, account: String, phone: String) {

        val database = FirebaseDatabase.getInstance().reference
        val userData = mapOf(
            "account" to account,
            "phone" to phone,
            "createTime" to System.currentTimeMillis()
        )

        database.child("users").child(uid).setValue(userData)
            .addOnSuccessListener {
                Toast.makeText(this, "註冊成功", Toast.LENGTH_SHORT).show()

                // 註冊成功 → 回登入頁或回主畫面
                startActivity(Intent(this, Login::class.java))
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "資料儲存失敗", Toast.LENGTH_SHORT).show()
            }
    }








    fun initialization(){
        //google初始化
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
        //fb初始化
        callbackManager = CallbackManager.Factory.create()
    }
    //設定監聽
    fun setlisteners() {
        //一般帳號註冊
        binding.registerBtregister.setOnClickListener {
            Account_Registration()
        }


        //google登入
        binding.btgoogleregister.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, GOOGLE_SIGN_IN)
        }
        //fb註冊
        binding.btfbregister.setOnClickListener {
            LoginManager.Companion.getInstance().logInWithReadPermissions(
                this,
                listOf("email", "public_profile")
            )
        }
        //line註冊未完成
        binding.btlineregister.setOnClickListener {
            lineLogin()
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Google Login
        if (requestCode == GOOGLE_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: Exception) {
                Toast.makeText(this, "Google 登入失敗", Toast.LENGTH_SHORT).show()
            }
        }
        // Facebook Login
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }
    private fun handleFacebookAccessToken(token: AccessToken) {
        val credential = FacebookAuthProvider.getCredential(token.token)

        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    saveUserToDatabase(FirebaseAuth.getInstance().currentUser)
                }
            }
    }
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)

        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = FirebaseAuth.getInstance().currentUser
                    saveUserToDatabase(user)
                } else {
                    Toast.makeText(this, "登入驗證失敗", Toast.LENGTH_SHORT).show()
                }
            }
    }
    private fun lineLogin() {

        val CHANNEL_ID = "1657918020"
        val REDIRECT_URI = "line3rdparty.com.example.dried_shrimp://oauth"

        val url =
            "intent://access.line.me/oauth2/v2.1/authorize" +
                    "?response_type=code" +
                    "&client_id=$CHANNEL_ID" +
                    "&redirect_uri=${URLEncoder.encode(REDIRECT_URI, "UTF-8")}" +
                    "&state=1234abcd" +
                    "&scope=profile%20openid%20email" +
                    "#Intent;package=jp.naver.line.android;end"

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        val uri = intent.data ?: return

        if (uri.scheme == "line3rdparty.com.example.dried_shrimp") {
            val code = uri.getQueryParameter("code")
            Toast.makeText(this, "LINE 回傳 code = $code", Toast.LENGTH_LONG).show()
        }
    }
    fun saveUserToDatabase(user: FirebaseUser?) {

    }

    fun saveUserToDatabaseWithLine(id: String?, name: String?) {

    }

    fun Login_page(){
        val tvLogin = binding.tvLogin
        val fullText = "已經有皮蝦帳號了嗎?登入"
        val spannable = SpannableString(fullText)
        val start = fullText.indexOf("登入")
        val end = start + 2 // "登入"兩個字
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                val intent = Intent(this@RegisterActivity, Login::class.java)
                startActivity(intent)
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false       // 是否要下底線（預設有）
                ds.color = Color.parseColor("#FF7F00") // 設定點擊文字顏色（可改）
            }
        }
        spannable.setSpan(clickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        tvLogin.text = spannable
        tvLogin.movementMethod = LinkMovementMethod.getInstance()
        tvLogin.highlightColor = Color.TRANSPARENT  // 點擊時不出現藍色底
    }
    fun back(){
        val back = binding.registerBack
        back.setOnClickListener {
            finish()
        }
    }
}