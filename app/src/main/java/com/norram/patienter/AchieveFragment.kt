package com.norram.patienter

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.SimpleAdapter
import androidx.databinding.DataBindingUtil
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.norram.patienter.databinding.FragmentAchieveBinding


class AchieveFragment : Fragment() {
    private var helper: AchieveOpenHelper? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = DataBindingUtil.inflate<FragmentAchieveBinding>(inflater,
            R.layout.fragment_achieve, container, false)

        if(helper == null) { helper = AchieveOpenHelper(requireContext()) }
        val achieveList = ArrayList<HashMap<String, String>>()
        val db = helper!!.writableDatabase
        val c = db.rawQuery("select sof, period, title from ACHIEVE_TABLE order by id DESC", null)

        try {
            // rawQueryというSELECT専用メソッドを使用してデータを取得する
            // Cursorの先頭行があるかどうか確認
            var next = c.moveToFirst()

            // 取得した全ての行を取得
            while (next) {
                val data = HashMap<String, String>()
                // 取得したカラムの順番(0から始まる)と型を指定してデータを取得する
                val sof = c.getString(0)
                val period = c.getString(1)
                var title = c.getString(2)
                if (title.length > 20) {
                    // リストに表示するのは20文字まで
                    title = title.substring(0, 21) + "..."
                }
                if(sof == getString(R.string.success)) {
                    data["sof"] = "〇"
                } else {
                    data["sof"] = "X"
                }
                data["period"] = period
                data["title"] = title
                achieveList.add(data)
                // 次の行が存在するか確認
                next = c.moveToNext()
            }
        } finally {
            db.close()
            c.close()
        }

        val simpleAdapter = SimpleAdapter(requireContext(),
            achieveList,
            R.layout.achieve_item,
            arrayOf("sof", "period", "title"), // どの項目を
            intArrayOf(R.id.sof, R.id.period, R.id.goalTitle) // どのidの項目に入れるか
        )
        binding.achieveList.adapter = simpleAdapter

        binding.backButton.setOnClickListener { view: View ->
            view.findNavController().navigateUp()
        }

        (activity as AppCompatActivity).supportActionBar?.hide()
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (activity as AppCompatActivity).supportActionBar?.show()
    }
}