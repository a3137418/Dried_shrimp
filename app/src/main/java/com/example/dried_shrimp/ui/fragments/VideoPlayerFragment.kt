package com.example.dried_shrimp.ui.fragments

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.dried_shrimp.FullScreenVideoView
import com.example.dried_shrimp.R

class VideoPlayerFragment : Fragment() {
    private var videoPath: String? = null
    // 將 videoView 提升為類別成員變數，以便在 onResume 和 onPause 中存取
    private var videoView: FullScreenVideoView? = null


    companion object {
        private const val KEY_VIDEO_PATH = "video_path"

        fun newInstance(path: String): VideoPlayerFragment {
            val f = VideoPlayerFragment()
            val bundle = Bundle()
            bundle.putString(KEY_VIDEO_PATH, path)
            f.arguments = bundle
            return f
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        videoPath = arguments?.getString(KEY_VIDEO_PATH)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.item_short_video, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        videoView = view.findViewById(R.id.videoView)

        if (videoView == null) {
            Log.e("VideoPlayerFragment", "VideoView with ID R.id.videoView not found in the layout.")
            return // 如果找不到元件，直接返回，避免崩潰
        }
        // 設定影片循環播放
        videoView?.setOnCompletionListener {
            it.seekTo(0)
            it.start()
        }
        videoPath?.let { path ->
            val uri = Uri.parse(path)
            videoView?.setVideoURI(uri)
            // *** 修正 #2：不要在這裡開始播放！將播放的職責完全交給 onResume ***
            // videoView?.start() // <--- 刪除或註解掉這一行
        }
    }
    override fun onResume() {
        super.onResume()
        // 在這裡開始或恢復播放
        if (videoView != null && videoView?.isPlaying == false) {
            Log.d("VideoPlayerFragment", "Resuming video: $videoPath")
            videoView?.seekTo(0)
            videoView?.start()
        }
    }
    override fun onPause() {
        super.onPause()
        // 在這裡暫停播放
        if (videoView != null && videoView?.isPlaying == true) {
            Log.d("VideoPlayerFragment", "Pausing video: $videoPath")
            videoView?.stopPlayback()
        }
    }
    // 當 Fragment 的視圖被銷毀時，釋放 videoView
    override fun onDestroyView() {
        super.onDestroyView()
        // 確保在視圖銷毀時也停止播放並釋放資源
        if (videoView != null) {
            videoView?.stopPlayback()
        }
        videoView = null
    }
}