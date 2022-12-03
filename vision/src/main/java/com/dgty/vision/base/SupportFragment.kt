package com.dgty.vision.base

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.SparseArray
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.viewbinding.ViewBinding
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.ToastUtils
import java.lang.reflect.ParameterizedType


abstract class SupportFragment<DB : ViewBinding> : Fragment() {

    val TAG = javaClass.name

    protected lateinit var mContext: Context

//    protected lateinit var viewModel: VM

    protected lateinit var mBinding: DB
    private var convertView: View? = null
    private var mViews = SparseArray<View>()

    //是否第一次加载
    private var isFirst: Boolean = true


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return initBinding(inflater, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        convertView = view
        onVisible()
        initView(savedInstanceState)
    }

    open fun initView(savedInstanceState: Bundle?) {}


    fun startActivity(clz: Class<out Activity>) {
        ActivityUtils.startActivity(clz)
    }


    /**
     * fragment中可以通过这个方法直接找到需要的view，而不需要进行类型强转
     * @param viewId
     * @param <E>
     * @return
    </E> */
    protected open fun <E : View> findView(viewId: Int): E? {
        convertView?.let {
            var view: E? = mViews.get(viewId) as E?
            if (view == null) {
                view = it.findViewById<View>(viewId) as E?
                mViews.put(viewId, view)
            }
            return view
        }
        return null
    }

    /**
     * 使用 DataBinding时,要重写此方法返回相应的布局 id
     * 使用ViewBinding时，不用重写此方法
     */
    open fun layoutId(): Int = 0

    /**
     * 是否需要懒加载
     */
    private fun onVisible() {
        if (lifecycle.currentState == Lifecycle.State.STARTED && isFirst) {
            lazyLoadData()
            isFirst = false
        }
    }

    /**
     * 懒加载
     */
    open fun lazyLoadData() {}

    /**
     * 注册 UI 事件
     */
    /*  private fun registorDefUIChange() {
          viewModel.defUI.showDialog.observe(viewLifecycleOwner, {
              showLoading()
          })
          viewModel.defUI.dismissDialog.observe(viewLifecycleOwner, {
              dismissLoading()
          })
          viewModel.defUI.toastEvent.observe(viewLifecycleOwner, {
             showInfo(it)
          })
          viewModel.defUI.msgEvent.observe(viewLifecycleOwner, {
              handleEvent(it)
          })
      }*/

    protected fun showInfo(info: String) {
        ToastUtils.getDefaultMaker().setMode(ToastUtils.MODE.DARK).setGravity(Gravity.TOP or Gravity.CENTER_HORIZONTAL, 0, 250).setTextSize(32).show(info)

    }


    /**
     * 打开等待框
     */
//    private fun showLoading() {
//        (dialog ?: MaterialDialog(requireContext())
//            .cancelable(false)
//            .cornerRadius(8f)
//            .customView(R.layout.custom_progress_dialog_view, noVerticalPadding = true)
//            .lifecycleOwner(this)
//            .maxWidth(literal = 40)
//            .also {
//                dialog = it
//            }).show()
//    }

    /**
     * 关闭等待框
     */
//    private fun dismissLoading() {
//        dialog?.run { if (isShowing) dismiss() }
//    }


    private fun initBinding(inflater: LayoutInflater, container: ViewGroup?): View {
        val type = javaClass.genericSuperclass
        if (type is ParameterizedType) {
            val cls = type.actualTypeArguments[1] as Class<*>
            return when {
                ViewDataBinding::class.java.isAssignableFrom(cls) && cls != ViewDataBinding::class.java -> {
                    if (layoutId() == 0) throw IllegalArgumentException("Using DataBinding requires overriding method layoutId")
                    mBinding = DataBindingUtil.inflate(inflater, layoutId(), container, false)
                    (mBinding as ViewDataBinding).lifecycleOwner = this
                    mBinding.root
                }
                ViewBinding::class.java.isAssignableFrom(cls) && cls != ViewBinding::class.java -> {
                    cls.getDeclaredMethod("inflate", LayoutInflater::class.java).let {
                        @Suppress("UNCHECKED_CAST") mBinding = it.invoke(null, inflater) as DB
                        mBinding.root
                    }
                }
                else -> {
                    if (layoutId() == 0) throw IllegalArgumentException("If you don't use ViewBinding, you need to override method layoutId")
                    inflater.inflate(layoutId(), container, false)
                }
            }
        } else throw IllegalArgumentException("Generic error")
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)

        this.mContext = context
    }

    override fun onResume() {
        super.onResume()
        onVisible()
    }


}