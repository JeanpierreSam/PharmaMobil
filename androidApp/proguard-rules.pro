# ===== Sesión 14: reglas mínimas para la versión de entrega =====

# R8 compiló esta versión sin reglas y sin missing_rules.txt (Ktor 3.6 y Tink no pidieron
# -dontwarn con AGP 9.0.1). Por eso no se agregan los -dontwarn previstos: solo lo necesario.
# kotlinx.serialization y Ktor traen sus propias reglas de consumidor.

# --- Crashlytics: trazas legibles (el plugin sube el mapping en cada build release) ---
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile