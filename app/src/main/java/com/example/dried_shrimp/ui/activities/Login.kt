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
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.dried_shrimp.R
import com.example.dried_shrimp.databinding.ActivityLoginBinding
import com.example.dried_shrimp.ui.activities.RegisterActivity
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import java.net.URLEncoder

class Login : AppCompatActivity() {
    lateinit var binding : ActivityLoginBinding
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var callbackManager: CallbackManager
    private val GOOGLE_SIGN_IN = 9001
    private lateinit var auth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Firebase Auth
        auth = FirebaseAuth.getInstance()
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


    private fun loginUser() {
        val email = binding.loginEtaccount.text.toString().trim()
        val password = binding.loginEtpasswword.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "請輸入帳號與密碼", Toast.LENGTH_SHORT).show()
            return
        }
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // ✅ 驗證成功，取得目前使用者
                    val user = auth.currentUser
                    Toast.makeText(this, "登入成功：${user?.email}", Toast.LENGTH_SHORT).show()

                    // 這裡可以跳到首頁或關閉登入頁
                    goToUser2()
                } else {
                    // ❌ 驗證失敗
                    val msg = task.exception?.message ?: "未知錯誤"
                    Toast.makeText(this, "登入失敗：$msg", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun goToUser2() {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("Login_successful", "fragment_user2")   // 給 MainActivity 的提示
        }
        startActivity(intent)
        finish()  // 通常關掉 Login 頁
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
        //一般登入
        binding.loginBtlogin.setOnClickListener {
            val account = binding.loginEtaccount.text.toString().trim()
            val password = binding.loginEtpasswword.text.toString().trim()
            loginUser()

        }
        //google登入
        binding.btgooglelogin.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, GOOGLE_SIGN_IN)
        }
        //fb登入
        LoginManager.Companion.getInstance().registerCallback(callbackManager, object :
            FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult) {
                Log.d("FB_LOGIN", "Facebook 登入成功，取得 Token")
                // 拿到 AccessToken 後，去換取 Firebase 憑證
                handleFacebookAccessToken(result.accessToken)
            }

            override fun onCancel() {
                Log.d("FB_LOGIN", "Facebook 登入取消")
                Toast.makeText(this@Login, "取消 FB 登入", Toast.LENGTH_SHORT).show()
            }

            override fun onError(error: FacebookException) {
                Log.e("FB_LOGIN", "Facebook 登入錯誤", error)
                Toast.makeText(this@Login, "FB 登入錯誤: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
        // 點擊按鈕觸發登入流程
        binding.btfblogin.setOnClickListener {
            LoginManager.Companion.getInstance().logInWithReadPermissions(
                this,
                listOf("email", "public_profile")
            )
        }
        //line登入未完成
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
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }

    //fb登入
    private fun handleFacebookAccessToken(token: AccessToken) {
        Log.d("FB_LOGIN", "開始進行 Firebase 憑證交換...") // 加入 Log 方便除錯
        val credential = FacebookAuthProvider.getCredential(token.token)

        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("FB_LOGIN", "Firebase 驗證成功")
                    // 登入成功，儲存資料並跳轉 (在 saveUserToDatabase 裡面做)
                    saveUserToDatabase(auth.currentUser)
                } else {
                    // ✅ 修正點：補上驗證失敗的處理
                    Log.e("FB_LOGIN", "Firebase 驗證失敗", task.exception)
                    Toast.makeText(this, "FB 登入失敗: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }
    //google登入
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)

        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    // 這裡 user 就是 Firebase 記住的使用者
                    Toast.makeText(this, "登入成功：${user?.email}", Toast.LENGTH_SHORT).show()

                    // 例如：回到主畫面或關閉 Login
                    goToUser2()
                } else {
                    Toast.makeText(this, "登入驗證失敗：${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
    //顯示FB_KEY_HASH
//    private fun printFacebookKeyHash() {
//        try {
//            val info = packageManager.getPackageInfo(
//                packageName,
//                PackageManager.GET_SIGNING_CERTIFICATES
//            )
//            val signatures = info.signingInfo!!.apkContentsSigners
//            for (signature in signatures) {
//                val md = MessageDigest.getInstance("SHA")
//                md.update(signature.toByteArray())
//                val keyHash = Base64.encodeToString(md.digest(), Base64.NO_WRAP)
//                Log.d("FB_KEY_HASH", "key hash = $keyHash")
//            }
//        } catch (e: Exception) {
//            Log.e("FB_KEY_HASH", "error: ${e.message}")
//        }
//    }
    fun Registration_page(){
        val tv_register = binding.tvRegister

        val fullText = "還沒有皮蝦帳號嗎?註冊"
        val spannable = SpannableString(fullText)

        val start = fullText.indexOf("註冊")
        val end = start + 2 // "註冊"兩個字

        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                val intent = Intent(this@Login, RegisterActivity::class.java)
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
        if (user == null) return

        val uid = user.uid
        val name = user.displayName ?: "皮蝦用戶"
        val email = user.email ?: ""
        val photoUrl = user.photoUrl?.toString() ?: ""
        val provider = user.providerData.firstOrNull { it.providerId != "firebase" }?.providerId ?: "unknown"

        val userData = mapOf(
            "uid" to uid,
            "displayName" to name,
            "email" to email,
            "photoUrl" to photoUrl,
            "provider" to provider,
            "lastLoginAt" to ServerValue.TIMESTAMP
        )

        val dbRef = FirebaseDatabase.getInstance().getReference("users").child(uid)
        dbRef.updateChildren(userData)
            .addOnSuccessListener {
                // ✅ 修正點：原本這裡只有 Toast，現在加上跳轉頁面
                Toast.makeText(this, "登入成功，資料已更新", Toast.LENGTH_SHORT).show()
                goToUser2()
            }
            .addOnFailureListener {
                // 如果資料庫寫入失敗，通常還是算登入成功，也可以選擇在這裡跳轉，或是提示錯誤
                Toast.makeText(this, "使用者資料儲存失敗，但仍允許進入", Toast.LENGTH_SHORT).show()
                goToUser2()
            }
    }


    fun back(){
        val back = binding.loginBack
        back.setOnClickListener {
            finish()
        }
    }

}