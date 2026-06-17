package dev.o0kam1.tools

import android.content.Intent
import android.view.View
import android.widget.PopupMenu
import io.github.duzhaokun123.module.base.ModuleEntry
import io.github.duzhaokun123.yabr.module.UICategory
import io.github.duzhaokun123.yabr.module.base.BaseModule
import io.github.duzhaokun123.yabr.module.base.SwitchModule
import io.github.duzhaokun123.yabr.module.base.UISwitch
import io.github.duzhaokun123.yabr.utils.ModuleEntryTarget
import io.github.duzhaokun123.yabr.utils.findField
import io.github.duzhaokun123.yabr.utils.getFieldValue
import io.github.duzhaokun123.yabr.utils.getFieldValueAs
import io.github.duzhaokun123.yabr.utils.getResId
import io.github.duzhaokun123.yabr.utils.loadClass
import io.github.duzhaokun123.yabr.utils.loadMethod

@ModuleEntry(
    id = "dev.o0kam1.tools.CommentPicture",
    targets = [ModuleEntryTarget.MAIN]
)
object CommentPicture : BaseModule(), UISwitch, SwitchModule {
    override val name = "评论图片查看"
    override val description = "评论图片三点菜单添加查看图片"
    override val category = UICategory.TOOL

    override fun onLoad(): Boolean {
        val class_CommentImageCardViewerDialogFragment =
            loadClass("com.bilibili.app.comment3.ui.widget.imagecardviewer.CommentImageCardViewerDialogFragment")
        val class_CardPagerManager = loadClass("com.bilibili.lib.imageviewer.card.CardPagerManager")
        val class_CardFragment = loadClass("com.bilibili.lib.imageviewer.card.CardFragment")
        val class_CommentImageCardFragment =
            loadClass("com.bilibili.app.comment3.ui.widget.imagecardviewer.CommentImageCardFragment")
        val class_CommentImageItem =
            loadClass("com.bilibili.app.comment3.ui.widget.imageviewer.CommentImageItem")
        val class_BaseMediaItem = loadClass("com.bilibili.lib.imageviewer.data.BaseMediaItem")

        val field_CommentImageCardViewerDialogFragment_cardPagerManager =
            class_CommentImageCardViewerDialogFragment
                .findField { it.type == class_CardPagerManager }
        val field_CardPagerManager_currentCardFragment =
            class_CardPagerManager
                .findField { it.type == class_CardFragment }
        val field_CommentImageCardFragment_currentImageItem =
            class_CommentImageCardFragment
                .findField { it.type == class_CommentImageItem }
        val field_BaseMediaItem_url =
            class_BaseMediaItem
                .findField { it.type == String::class.java }

        loadMethod("Lcom/bilibili/app/comment3/ui/widget/imagecardviewer/CommentImageCardViewerDialogFragment;->initView(Landroid/view/View;)V")
            .hookAfter { hook ->
                val view = hook.args[0] as View
                val moreButton = view.findViewById<View>(getResId("more_button"))
                val originOnClickListener = moreButton
                    .getFieldValueAs<Any>("mListenerInfo")
                    .getFieldValueAs<View.OnClickListener>("mOnClickListener")

                moreButton.setOnClickListener { v ->
                    PopupMenu(v.context, v).apply {
                        menu.add("查看图片").setOnMenuItemClickListener {
                            val pagerManager = hook.thiz!!
                                .getFieldValueAs<Any>(
                                    field_CommentImageCardViewerDialogFragment_cardPagerManager
                                )
                            val currentCard = pagerManager
                                .getFieldValueAs<Any>(field_CardPagerManager_currentCardFragment)
                            val currentImage = currentCard
                                .getFieldValue(field_CommentImageCardFragment_currentImageItem)!!
                            val url = currentImage.getFieldValueAs<String>(field_BaseMediaItem_url)

                            logger.d("comment picture url=$url")

                            v.context.startActivity(
                                Intent(
                                    v.context, PhotoActivity::class.java
                                ).apply {
                                    putExtra(PhotoActivity.URL, url)
                                })
                            true
                        }
                        menu.add("分享").setOnMenuItemClickListener {
                            originOnClickListener.onClick(v)
                            true
                        }
                    }.show()
                }
            }
        return true
    }
}
