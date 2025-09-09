// KOTLIN: 'package' define un "espacio de nombres" para organizar tu código.
// Debe coincidir con el nombre del paquete que definiste al crear el proyecto.
package com.alpes.mantenimientoapp // <- ¡ASEGÚRATE DE QUE ESTE SEA TU PAQUETE!

// ANDROID: 'import' trae herramientas o "planos" pre-hechos que necesitamos para construir la app.
// Piensa en ellos como si importaras librerías de funciones en otros lenguajes.
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alpes.mantenimientoapp.ui.theme.MantenimientoAppTheme // <- ¡REVISA QUE ESTA LÍNEA COINCIDA!

// KOTLIN: 'class MainActivity' es la definición de nuestra pantalla principal.
// ANDROID: Hereda de 'ComponentActivity', que es una clase base de Android que sabe cómo manejar una pantalla.
class MainActivity : ComponentActivity() {
    // ANDROID: 'override fun onCreate' es una función que se ejecuta automáticamente
    // cuando se crea esta pantalla por primera vez. Es el punto de partida.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // ANDROID: 'setContent' es donde le decimos a Android que el contenido
        // de esta pantalla será dibujado usando Jetpack Compose.
        setContent {
            // ANDROID/COMPOSE: 'Creatic_AppTheme' es un componente que aplica los estilos
            // generales (colores, fuentes) que definiremos más adelante.
            MantenimientoAppTheme {
                // ANDROID/COMPOSE: 'Surface' es un contenedor básico que le da un fondo a la UI.
                Surface(
                    modifier = Modifier.fillMaxSize(), // El modificador le dice que ocupe toda la pantalla.
                    color = Color(0xFF33A8FF) // Un color de fondo azul, similar a tu imagen.
                ) {
                    LoginScreen() // Llamamos a nuestra función que dibuja el formulario.
                }
            }
        }
    }
}

// ANDROID/COMPOSE: '@Composable' es una anotación mágica. Le dice a Android que esta
// función no es para hacer cálculos, sino para describir una parte de la interfaz de usuario.
@Composable
fun LoginScreen() {
    // KOTLIN: 'var usuario by remember { mutableStateOf("") }' declara una variable especial.
    // 'var': Es una variable que puede cambiar (a diferencia de 'val').
    // 'remember': Le dice a Compose que "recuerde" el valor de esta variable incluso si la pantalla se redibuja.
    // 'mutableStateOf("")': Crea un estado observable. Cuando su valor cambia, Compose redibuja
    // automáticamente las partes de la pantalla que usan esta variable. Inicialmente está vacío ("").
    var usuario by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }

    // ANDROID/COMPOSE: 'Box' es un contenedor que permite apilar elementos uno encima de otro.
    // Lo usamos aquí para centrar fácilmente la tarjeta de login en medio de la pantalla.
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center // Alinea el contenido en el centro.
    ) {
        // ANDROID/COMPOSE: 'Card' es un contenedor con sombra y esquinas redondeadas, ideal para formularios.
        Card(
            shape = RoundedCornerShape(20.dp), // Define qué tan redondeadas son las esquinas.
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.padding(24.dp) // Añade un espacio alrededor de la tarjeta.
        ) {
            // ANDROID/COMPOSE: 'Column' es un contenedor que apila elementos uno debajo del otro, verticalmente.
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally // Centra todos los elementos horizontalmente.
            ) {
                // ANDROID/COMPOSE: 'Image' muestra una imagen.
                // IMPORTANTE: Debes añadir tu logo a la carpeta 'res/drawable'.
                // Por ejemplo, si tu logo se llama 'creatic_logo.png', la línea sería:
                // painter = painterResource(id = R.drawable.creatic_logo),
                Image(
                    painter = painterResource(id = R.drawable.logo), // ¡CAMBIA ESTO POR TU LOGO!
                    contentDescription = "Logo de la empresa",
                    modifier = Modifier.height(120.dp).size(100.dp)
                )

                // ANDROID/COMPOSE: 'Spacer' simplemente crea un espacio vacío entre elementos.
                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider(color = Color(0xFFF57C00)) // Añade una línea divisoria naranja.
                Spacer(modifier = Modifier.height(16.dp))

                // ANDROID/COMPOSE: 'Text' muestra un texto.
                Text(text = "INICIAR SESIÓN", fontSize = 20.sp, style = MaterialTheme.typography.titleLarge)

                Spacer(modifier = Modifier.height(16.dp))

                // ANDROID/COMPOSE: 'OutlinedTextField' es un campo de texto con un borde.
                OutlinedTextField(
                    value = usuario, // KOTLIN: El texto a mostrar es el valor de nuestra variable 'usuario'.
                    onValueChange = { usuario = it }, // KOTLIN: Cuando el usuario escribe, actualiza la variable.
                    label = { Text("Usuario") },
                    modifier = Modifier.fillMaxWidth() // Le dice que ocupe todo el ancho disponible.
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = contrasena,
                    onValueChange = { contrasena = it },
                    label = { Text("Contraseña") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation() // Esto hace que se vean '*' en lugar de texto.
                )

                Spacer(modifier = Modifier.height(8.dp))

                // ANDROID/COMPOSE: 'TextButton' es un botón que parece un texto simple.
                TextButton(onClick = { /* TODO: Lógica para recuperar contraseña */ }) {
                    Text(
                        text = "Recuperar contraseña",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center, // Alinea el texto a la derecha.
                        color = Color(0xFFF57C00)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ANDROID/COMPOSE: 'Button' es el botón principal.
                Button(
                    onClick = { /* TODO: Lógica para validar el login cuando se presione */ },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF57C00)) // Color naranja.
                ) {
                    Text(text = "Ingresar", fontSize = 18.sp, color = Color.White)
                }
            }
        }
    }
}

// ANDROID/COMPOSE: '@Preview' le permite a Android Studio mostrarte una vista previa de tu
// diseño sin tener que ejecutar la app en un emulador o teléfono. Es muy útil.
@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MantenimientoAppTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFF33A8FF)
        ) {
            LoginScreen()
        }
    }
}