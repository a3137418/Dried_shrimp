package com.example.dried_shrimp

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
import com.example.dried_shrimp.databinding.ActivityLoginBinding
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import java.net.URLEncoder


class Login : AppCompatActivity() {
    lateinit var binding : ActivityLoginBinding
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var callbackManager: CallbackManager
    private val GOOGLE_SIGN_IN = 9001


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // 讓底部認證區往上推
        // 上方 header 避免與狀態列重疊
        ViewCompat.setOnApplyWindowInsetsListener(binding.loginHeader) { view, insets ->
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
        Registration_page()
        initialization()

        setlisteners()
        back()

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
        binding.btgooglelogin.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, GOOGLE_SIGN_IN)
        }
        binding.btfblogin.setOnClickListener {
            LoginManager.getInstance().logInWithReadPermissions(
                this,
                listOf("email", "public_profile")
            )
        }

        binding.btlinelogin.setOnClickListener {
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

    //fb接收
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


    fun Registration_page(){
        val tv_register = binding.tvRegister

        val fullText = "還沒有皮蝦帳號嗎?註冊"
        val spannable = SpannableString(fullText)

        val start = fullText.indexOf("註冊")
        val end = start + 2 // "註冊"兩個字

        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                val intent = Intent(this@Login, register::class.java)
                startActivity(intent)
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false       // 是否要下底線（預設有）
                ds.color = Color.parseColor("#FF7F00") // 設定點擊文字顏色（可改）
            }
        }
        spannable.setSpan(clickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        tv_register.text = spannable
        tv_register.movementMethod = LinkMovementMethod.getInstance()
        tv_register.highlightColor = Color.TRANSPARENT  // 點擊時不出現藍色底
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

    fun back(){
        val back = binding.loginBack
        back.setOnClickListener {
            finish()
        }
    }

}