#-dontnote
-ignorewarnings
#-dontobfuscate
#-dontoptimize
-verbose

-libraryjars  C:/Program Files/Java/jdk-11.0.6/jmods/java.base.jmod(!**.jar;!module-info.class)
-libraryjars  C:/Program Files/Java/jdk-11.0.6/jmods/java.desktop.jmod(!**.jar;!module-info.class)
-libraryjars  C:/Program Files/Java/jdk-11.0.6/jmods/java.xml.jmod(!**.jar;!module-info.class)
-libraryjars  C:/Program Files/Java/javafx/jmods/javafx.base.jmod(!**.jar;!module-info.class)
-libraryjars  C:/Program Files/Java/javafx/jmods/javafx.fxml.jmod(!**.jar;!module-info.class)
-libraryjars  C:/Program Files/Java/javafx/jmods/javafx.controls.jmod(!**.jar;!module-info.class)
-libraryjars  C:/Program Files/Java/javafx/jmods/javafx.graphics.jmod(!**.jar;!module-info.class)

-keepattributes Annotation, SourceFile, LineNumberTable

-keep public class com.ffmpeg.Main {
    public static void main(java.lang.String[]);
}

# ServiceLoader support
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepnames class kotlinx.coroutines.android.AndroidExceptionPreHandler {}
-keepnames class kotlinx.coroutines.android.AndroidDispatcherFactory {}

# Most of volatile fields are updated with AFU and should not be mangled
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

# Same story for the standard library's SafeContinuation that also uses AtomicReferenceFieldUpdater
-keepclassmembernames class kotlin.coroutines.SafeContinuation {
    volatile <fields>;
}

-keep class * {
    @javafx.fxml.FXML *;
}

-dontwarn kotlinx.coroutines.**
-optimizations !code/allocation/variable