package smu.it.ips2

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import smu.it.ips2.databinding.ActivityJoinBinding

class JoinActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    private val CHECK_POLICY_REQUEST = 1
    private lateinit var binding : ActivityJoinBinding
    private lateinit var database : FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {

        // Initialize Firebase Auth
        auth = Firebase.auth

        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_join)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        binding.joinBtn.isEnabled = false

        binding.checkPolicyBtn.setOnClickListener {
            intent = Intent(this, CheckPolicyActivity::class.java)
            startActivityForResult(intent, CHECK_POLICY_REQUEST)
        }

        fun joinAccount() {
            var join = true
            var name = binding.inputName.text.toString()
            var id = binding.inputID.text.toString()
            var password = binding.inputPWD.text.toString()
            var passwordCheck = binding.checkPWD.text.toString()

            if (name.isEmpty()) {
                Toast.makeText(this, "이름을 입력해주세요", Toast.LENGTH_SHORT).show()
                join = false
            } else if (id.isEmpty()) {
                Toast.makeText(this, "아이디를 입력해주세요", Toast.LENGTH_LONG).show()
                join = false
            } else if (password.isEmpty()) {
                Toast.makeText(this, "비밀번호를 입력해주세요", Toast.LENGTH_LONG).show()
                join = false
            } else if (passwordCheck.isEmpty()) {
                Toast.makeText(this, "비밀번호를 확인해주세요", Toast.LENGTH_LONG).show()
                join = false
            } else if (password != passwordCheck) { // 비밀번호 2개가 같은지 확인
                Toast.makeText(this, "비밀번호를 똑같이 입력해주세요", Toast.LENGTH_LONG).show()
                join = false
            } else if (password.length < 8) {   // 비밀번호가 8자 이상인지
                Toast.makeText(this, "비밀번호를 8자리 이상으로 입력해주세요", Toast.LENGTH_LONG).show()
                join = false
            }


            if (join) {
                auth.createUserWithEmailAndPassword(id, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            val user = auth.currentUser
                            val userId = user!!.uid

                            val userObject = HashMap<String, String>()
                            userObject["name"] = name
                            userObject["email"] = id

                            val ref = database.getReference("users/$userId/name")
                            Toast.makeText(this, "[test message] OK(1)", Toast.LENGTH_LONG).show()
                            ref.setValue(userObject).addOnSuccessListener {
                                Toast.makeText(this, "회원가입에 성공하였습니다.", Toast.LENGTH_LONG).show()
                                val intent = Intent(this, MainActivity::class.java)
                                intent.flags =
                                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                                startActivity(intent)
                                finish() // JoinActivity 종료
                            }
                        }
                        else if (task.exception is FirebaseAuthUserCollisionException) {
                            binding.checkIDText.visibility = View.VISIBLE
                            Toast.makeText(this, "사용불가능한 아이디입니다.", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(this, "아이디를 이메일 형식으로 정확히 입력해주세요.", Toast.LENGTH_LONG).show()
                        }
                    }
            }
        }


        binding.joinBtn.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // JoinActivity 종료
            joinAccount()
        }

        binding.exitBtn.setOnClickListener {
            intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        val spinner = binding.selectAge
        spinner.adapter = ArrayAdapter.createFromResource(
            this,
            R.array.ageList,
            android.R.layout.simple_spinner_item
        )
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (position > 0) {
                    val selectedAge = parent?.getItemAtPosition(position).toString()

                    val intent = Intent(this@JoinActivity, MyPageActivity::class.java)
                    intent.putExtra("userAge", selectedAge)

                    Toast.makeText(
                        this@JoinActivity,
                        "나이가 $selectedAge 로 선택되었어요!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CHECK_POLICY_REQUEST && resultCode == Activity.RESULT_OK) {
            binding.joinBtn.isEnabled = true
            binding.joinBtn.setImageResource(R.drawable.joinafterbtn)
        }
    }

}