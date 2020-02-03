package com.ffmpeg.controllers

import com.ffmpeg.Main
import com.ffmpeg.models.Command
import com.ffmpeg.models.FFmpeg
import com.ffmpeg.models.codec.*
import com.ffmpeg.util.FFLogger
import com.ffmpeg.util.Preferences
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.*
import javafx.scene.layout.Pane
import javafx.stage.FileChooser
import javafx.stage.Stage
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.javafx.JavaFx
import java.io.File
import java.net.URL
import java.util.*

@ExperimentalCoroutinesApi
class MainAppController : Initializable, CoroutineScope {

    private val job = Job()
    override val coroutineContext = Dispatchers.JavaFx + job

    /* fxml */
    // media
    @FXML lateinit var rootPane: Pane
    @FXML lateinit var browseButton: Button
    @FXML lateinit var convertButton: Button
    @FXML lateinit var startText: TextField
    @FXML lateinit var endText: TextField
    @FXML lateinit var currentProgress: ProgressBar
    @FXML lateinit var totalProgress: ProgressBar
    @FXML lateinit var currentLabel: Label
    // video
    @FXML lateinit var videoCodecsCombo: ComboBox<VideoCodec>
    @FXML lateinit var widthSpinner: Spinner<Int>
    @FXML lateinit var heightSpinner: Spinner<Int>
    @FXML lateinit var videoBiterateSpinner: Spinner<Int>
    @FXML lateinit var presetsCombo: ChoiceBox<String>
    @FXML lateinit var qualitySlider: Slider
    @FXML lateinit var videoDisabledCheck: CheckBox
    // audio
    @FXML lateinit var audioCodecsCombo: ComboBox<AudioCodec>
    @FXML lateinit var audioBiterateSpinner: Spinner<Int>
    @FXML lateinit var audioDisabledCheck: CheckBox

    // fields
    private val codecs = Codecs()
    private var files: List<File>? = null
    private var isWorking = false

    // properties
    private val videoCodecs = FXCollections.observableArrayList<VideoCodec>(codecs.videoCodecs)
    private val audioCodecs = FXCollections.observableArrayList<AudioCodec>(codecs.audioCodecs)
    private val presets = FXCollections.observableArrayList<String>(codecs.codecPresets)
    private val start = SimpleStringProperty()
    private val end = SimpleStringProperty()
    private val videoDisable = SimpleBooleanProperty()
    private val audioDisable = SimpleBooleanProperty()
    private val quality = SimpleDoubleProperty()
    private val currentFile = SimpleStringProperty()
    private val currentConvert = SimpleDoubleProperty()
    private val totalConvert = SimpleDoubleProperty()
    private val convertText = SimpleStringProperty("Convert")

    private val stage get() = rootPane.scene.window as Stage
    private var command = Command()

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        videoCodecsCombo.items = videoCodecs
        audioCodecsCombo.items = audioCodecs
        presetsCombo.items = presets
        startText.textProperty().bindBidirectional(start)
        endText.textProperty().bindBidirectional(end)
        widthSpinner.valueFactory = SpinnerValueFactory.IntegerSpinnerValueFactory(0, 3840, 0, 1)
        heightSpinner.valueFactory = SpinnerValueFactory.IntegerSpinnerValueFactory(0, 2160, 0, 1)
        videoBiterateSpinner.valueFactory = SpinnerValueFactory.IntegerSpinnerValueFactory(0, Int.MAX_VALUE, 0, 1)
        audioBiterateSpinner.valueFactory = SpinnerValueFactory.IntegerSpinnerValueFactory(0, Int.MAX_VALUE, 0, 1)
        qualitySlider.valueProperty().bindBidirectional(quality)
        currentProgress.progressProperty().bind(currentConvert)
        totalProgress.progressProperty().bind(totalConvert)
        currentLabel.textProperty().bind(currentFile)
        convertButton.textProperty().bind(convertText)
        videoDisabledCheck.selectedProperty().bindBidirectional(videoDisable)
        audioDisabledCheck.selectedProperty().bindBidirectional(audioDisable)
        browseButton.setOnMouseClicked { onBrowse() }
        convertButton.setOnMouseClicked {
            if (isWorking) {
                reInitialize()
            } else {
                convertText.value = "Stop"
                onConvert()
            }
        }
        launch {
            stage.setOnCloseRequest {
                job.cancel()
                command.close()
            }
        }
    }

    private fun reInitialize() {
        job.cancelChildren()
        command.close()
        convertText.value = "Convert"
        currentConvert.value = 0.0
        totalConvert.value = 0.0
        currentFile.value = ""
        isWorking = false
    }

    private fun onBrowse() {
        val extensions = listOf("mp4", "mp3", "mkv", "wav", "ogg", "webm", "gif", "wmv", "m4a", "avi", "mov", "flac", "ts")
            .map { "*.$it" }
        var pref: String by Preferences(Main::class.java.name).get<String>("default_folder")
        launch {
            val file = async(Dispatchers.IO) { File(pref).exists() }
            val lastFile = withTimeoutOrNull(1000) { file.await() }

            if (lastFile == null) FFLogger.warning("Couldn't open the last file")
            files = FileChooser().apply {
                extensionFilters.add(FileChooser.ExtensionFilter("Media File", extensions))
                lastFile?.let { if (lastFile) initialDirectory = File(pref) }
            }.showOpenMultipleDialog(stage)
            files?.let {
                pref = it[0].parent
                FFLogger.info("Current folder location is now [$pref]")
            }
        }
    }

    private fun onConvert() {
        val params = mutableListOf<Param?>()

        params += videoCodecsCombo.selectionModel.selectedItem
        params += audioCodecsCombo.selectionModel.selectedItem
        params += start.value?.let { if (it.isNotBlank()) SimpleParam("-ss", it) else null }
        params += end.value?.let { if (it.isNotBlank()) SimpleParam("-to", it) else null }
        params += if (widthSpinner.value != 0 || heightSpinner.value != 0) {
            val width = if (widthSpinner.value != 0) widthSpinner.value else -1
            val height = if (heightSpinner.value != 0) heightSpinner.value else -1
            SimpleParam("-vf", "scale=$width:$height")
        } else {
            null
        }
        params += if (videoBiterateSpinner.value != 0) SimpleParam("-b:v", videoBiterateSpinner.value.toString()) else null
        params += if (audioBiterateSpinner.value != 0) SimpleParam("-b:a", audioBiterateSpinner.value.toString()) else null
        params += if (videoDisable.value) SimpleParam("-vn", "") else null
        params += if (audioDisable.value) SimpleParam("-an", "") else null
        params += presetsCombo.selectionModel.selectedItem?.let { SimpleParam("-preset", it.toLowerCase()) }
        params += if (quality.value > 0.0) {
            videoCodecsCombo.selectionModel.selectedItem?.let { SimpleParam(it.qualityKey, "${quality.value.toInt()}") }
        } else {
            null
        }
        isWorking = true
        startConvert(params.filterNotNull())
    }

    private fun startConvert(params: List<Param>) {
        val ffmpeg = FFmpeg("ffmpeg")

        launch {
            val convertFiles = files?.map { file ->
                val duration = async(Dispatchers.IO) {
                    try {
                        ffmpeg.duration(file, params.find { it.key == "-ss" }, params.find { it.key == "-to" })
                    } catch (e: NoSuchElementException) {
                        FFLogger.severe(e.message)
                        null
                    }
                }

                file to duration
            }
                ?.filter { it.second.await() != null }
                ?.map { it.first to it.second.await()!! }
            val totalDuration = convertFiles?.sumBy { it.second }
            var overall = 0.0

            convertFiles?.forEach { (file, duration) ->
                val fileName = file.name

                ffmpeg.convert(file, params, command).collect { (current, eta) ->
                    currentConvert.value = current.toDouble() / duration
                    totalConvert.value = (overall + current) / totalDuration!!
                    currentFile.value = "${fileName.ellipse} - ${eta.toTimeString}"
                }
                overall += duration
                FFLogger.info("${file.name} converted successfully!")
            }
        }.invokeOnCompletion { reInitialize() }

    }

    private val Int.toTimeString get() = if (this > 60) "${this / 60}m${this % 60}s" else "${this % 60}s"

    private val String.ellipse get() = if (length > 50) {
        "${substring(0, 50 / 2)}[...]${substring(length - 50 / 2, length)}"
    } else {
        this
    }
}