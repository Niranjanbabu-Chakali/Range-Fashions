package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.data.UserProfile
import com.example.ui.MainViewModel
import com.example.ui.theme.*

@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: MainViewModel,
    onShowSnackbar: (String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BrandWhite)
            .verticalScroll(scrollState)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        // Brand Banner
        Text(
            text = "RANGE",
            fontSize = 36.sp,
            fontWeight = FontWeight.ExtraBold,
            color = BrandBlack,
            letterSpacing = 6.sp,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Text(
            text = "WEAR THE RANGE",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray,
            letterSpacing = 3.sp,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Text(
            text = "Welcome Back",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = BrandBlack,
            modifier = Modifier.align(Alignment.Start)
        )
        Text(
            text = "Sign in to access your premium cart, orders, and wishlist.",
            fontSize = 11.sp,
            color = Color.Gray,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(bottom = 24.dp)
        )

        // Email field
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email Address", fontSize = 12.sp) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            shape = RoundedCornerShape(0.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = BrandBlack,
                unfocusedTextColor = BrandBlack,
                focusedBorderColor = BrandBlack,
                unfocusedBorderColor = BrandBorderGrey
            )
        )

        // Password field
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password", fontSize = 12.sp) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            shape = RoundedCornerShape(0.dp),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = "Toggle password visibility"
                    )
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = BrandBlack,
                unfocusedTextColor = BrandBlack,
                focusedBorderColor = BrandBlack,
                unfocusedBorderColor = BrandBorderGrey
            )
        )

        // Forgot password
        Text(
            text = "Forgot Password?",
            fontSize = 11.sp,
            color = BrandAccentRed,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.End)
                .padding(vertical = 8.dp)
                .clickable {
                    onShowSnackbar("Password reset link sent to your email address.")
                }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Sign In Button
        Button(
            onClick = {
                if (email.trim().isNotEmpty() && password.isNotEmpty()) {
                    // Create mock profile
                    val profile = UserProfile(
                        fullName = "Dapper Customer",
                        email = email.trim(),
                        phone = "9876543210",
                        gender = "Male",
                        dob = "01/01/1998",
                        profilePicUrl = ""
                    )
                    viewModel.updateProfile(profile)
                    onShowSnackbar("Signed in successfully!")
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                } else {
                    onShowSnackbar("Please enter a valid email and password.")
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BrandBlack, contentColor = BrandWhite),
            shape = RoundedCornerShape(0.dp)
        ) {
            Text("SIGN IN", fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Switch to register link
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text("Don't have an account? ", fontSize = 12.sp, color = Color.Gray)
            Text(
                text = "Sign Up",
                fontSize = 12.sp,
                color = BrandBlack,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable {
                    navController.navigate("register")
                }
            )
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun RegisterScreen(
    navController: NavController,
    viewModel: MainViewModel,
    onShowSnackbar: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BrandWhite)
            .verticalScroll(scrollState)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "RANGE",
            fontSize = 36.sp,
            fontWeight = FontWeight.ExtraBold,
            color = BrandBlack,
            letterSpacing = 6.sp,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Text(
            text = "CREATE ACCOUNT",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray,
            letterSpacing = 3.sp,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Name
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Full Name", fontSize = 12.sp) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            shape = RoundedCornerShape(0.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = BrandBlack,
                unfocusedTextColor = BrandBlack,
                focusedBorderColor = BrandBlack,
                unfocusedBorderColor = BrandBorderGrey
            )
        )

        // Email
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email Address", fontSize = 12.sp) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            shape = RoundedCornerShape(0.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = BrandBlack,
                unfocusedTextColor = BrandBlack,
                focusedBorderColor = BrandBlack,
                unfocusedBorderColor = BrandBorderGrey
            )
        )

        // Password
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password", fontSize = 12.sp) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            shape = RoundedCornerShape(0.dp),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = "Toggle password visibility"
                    )
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = BrandBlack,
                unfocusedTextColor = BrandBlack,
                focusedBorderColor = BrandBlack,
                unfocusedBorderColor = BrandBorderGrey
            )
        )

        // Confirm Password
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm Password", fontSize = 12.sp) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            shape = RoundedCornerShape(0.dp),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = BrandBlack,
                unfocusedTextColor = BrandBlack,
                focusedBorderColor = BrandBlack,
                unfocusedBorderColor = BrandBorderGrey
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Register Button
        Button(
            onClick = {
                if (name.trim().isEmpty() || email.trim().isEmpty() || password.isEmpty()) {
                    onShowSnackbar("Please fill all fields.")
                } else if (password != confirmPassword) {
                    onShowSnackbar("Passwords do not match.")
                } else {
                    val profile = UserProfile(
                        fullName = name.trim(),
                        email = email.trim(),
                        phone = "9876543210",
                        gender = "Not specified",
                        dob = "01/01/1998",
                        profilePicUrl = ""
                    )
                    viewModel.updateProfile(profile)
                    onShowSnackbar("Registered successfully!")
                    navController.navigate("home") {
                        popUpTo("register") { inclusive = true }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BrandBlack, contentColor = BrandWhite),
            shape = RoundedCornerShape(0.dp)
        ) {
            Text("REGISTER", fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Switch to login link
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text("Already have an account? ", fontSize = 12.sp, color = Color.Gray)
            Text(
                text = "Sign In",
                fontSize = 12.sp,
                color = BrandBlack,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable {
                    navController.navigate("login")
                }
            )
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}
