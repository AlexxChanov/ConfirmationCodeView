package com.chanov.mylibrary

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat

class ConfirmationCodeView : AppCompatEditText {

  companion object {
    const val EMPTY_SIGN = '*'
  }

  interface OnCodeEnteredListener {
    fun onCodeEntered(code: String)
  }

  interface OnCodeAlertChangedListener {
    fun onCodeAlertChanged(isAlerted: Boolean)
  }

  private var boxStrokeWidth: Int = 0
  private var rectRadius: Int = 0
  private var rectWidth: Int = 0
  private var digitWidth: Int = 0
  private var digitHeight: Int = 0
  private var digitExtraSpace: Int = 0
  private var digitNumber: Int = 1
  private var alertColor = ContextCompat.getColor(context, R.color.teal_200)
  private var primaryColor = ContextCompat.getColor(context, R.color.purple_200)
  private var chars = createChars()
  private var strokePaint = Paint().apply {
    style = Paint.Style.STROKE
  }

  private lateinit var backgroundPaint: Paint

  private var isAlerted: Boolean = false
    set(value) {
      field = value
      onCodeAlertChangedListener?.onCodeAlertChanged(field)
    }
  private var isBlocked: Boolean = false
    set(value) {
      field = value
      isEnabled = !field
      if (isEnabled) {
        requestFocusAndShowKeyboard()
      }
    }

  var onCodeEnteredListener: OnCodeEnteredListener? = null
  var onCodeAlertChangedListener: OnCodeAlertChangedListener? = null

  constructor(context: Context) : super(context)

  constructor(
    context: Context,
    attributeSet: AttributeSet,
  ) : super(context, attributeSet) {
    init(context, attributeSet)
  }

  fun alertPin() {
    isAlerted = true
    invalidate()
  }

  constructor(
    context: Context,
    attributeSet: AttributeSet,
    defStyleAttr: Int,
  ) : super(context, attributeSet, defStyleAttr) {
    init(context, attributeSet)
  }

  private fun init(
    context: Context,
    attributeSet: AttributeSet,
  ) {
    parseAttributes(context, attributeSet)

    setPadding(0, 0, 0, 0)

    addTextChangedListener(CodeEntryTextWatcher())

    setOnClickListener {
      requestFocusAndShowKeyboard()
      setSelection(
        findLastCharsIndex(chars, digitNumber)
      )
    }
  }

  private fun parseAttributes(
    context: Context,
    attributeSet: AttributeSet,
  ) {
    val a = context.theme.obtainStyledAttributes(
      attributeSet,
      R.styleable.confirmationCodeView,
      0, 0
    )

    try {
      boxStrokeWidth = a.getDimensionPixelSize(R.styleable.confirmationCodeView_boxStrokeWidth, 0)
      rectRadius = a.getDimensionPixelSize(R.styleable.confirmationCodeView_rectRadius, 0)
      rectWidth = a.getDimensionPixelSize(R.styleable.confirmationCodeView_rectWidth, 0)
      digitWidth = a.getDimensionPixelSize(R.styleable.confirmationCodeView_digitWidth, 0)
      digitHeight = a.getDimensionPixelSize(R.styleable.confirmationCodeView_digitHeight, 0)
      digitNumber = a.getInt(R.styleable.confirmationCodeView_digitNumber, 1)
      filters += InputFilter.LengthFilter(digitNumber)
      digitExtraSpace =
        a.getDimensionPixelSize(R.styleable.confirmationCodeView_digitExtraSpace, 0)
      background = ResourcesCompat.getDrawable(resources, R.color.transparent, null)
      alertColor = a.getColor(
        R.styleable.confirmationCodeView_alertColor,
        ContextCompat.getColor(context, R.color.teal_200),
      )
      strokePaint.strokeWidth = boxStrokeWidth.toFloat()
      strokePaint.color = a.getColor(
        R.styleable.confirmationCodeView_strokeColor,
        ContextCompat.getColor(context, R.color.purple_200),
      )
      chars = createChars()
      width =
        rectWidth * digitNumber + digitExtraSpace * (digitNumber + 1)
      height = digitHeight
      paint.textAlign = Paint.Align.CENTER

      backgroundPaint = Paint().apply {
        color = Color.WHITE
      }

    } finally {
      a.recycle()
    }
  }

  fun clear() {
    chars = createChars()
    setText("")
  }

  private fun createChars() = CharArray(digitNumber) { EMPTY_SIGN }

  fun block(isBlockMode: Boolean) {
    isBlocked = isBlockMode
  }

  private fun onTextChanged() {
    if (isAlerted && chars.indexOf(EMPTY_SIGN) != 0) {
      isAlerted = false
    }
    invalidate()
    if (chars.lastIndexOf(EMPTY_SIGN) == -1) {
      onCodeEnteredListener?.onCodeEntered(getCode())
    }
  }

  override fun onDraw(canvas: Canvas) {
    if (isAlerted) {
      paint.color = alertColor
    } else {
      paint.color = currentTextColor
    }

    var start = paddingLeft + digitExtraSpace
    var startDigit = paddingLeft + digitExtraSpace + rectWidth / 2
    val textY = height / 2 - (paint.ascent() + paint.descent()) / 2

    val lastCharIndex = findLastCharsIndex(chars, 0)

    for (i in 0.until(chars.size)) {
      startDigit += if (i > 0) rectWidth + digitExtraSpace else 0

      start += if (i > 0) rectWidth + digitExtraSpace else 0

      canvas.drawRoundRect(
        start.toFloat(),
        (boxStrokeWidth / 2).toFloat(),
        (start + rectWidth).toFloat(),
        height.toFloat() - boxStrokeWidth / 2,
        rectRadius.toFloat(),
        rectRadius.toFloat(),
        backgroundPaint
      )

      if (i == lastCharIndex) {
        drawRectStroke(canvas, start)
      }

      if (chars[i] != EMPTY_SIGN) {
        canvas.drawText(chars[i].toString(), startDigit.toFloat(), textY, paint)
      }
    }
  }

  private fun findLastCharsIndex(
    chars: CharArray,
    defValue: Int,
  ): Int {
    return chars.indexOf(EMPTY_SIGN)
      .takeIf { it != -1 }
      .orDefault(defValue)
      .coerceAtMost(length())
  }

  private fun drawRectStroke(
    canvas: Canvas,
    start: Int,
  ) {
    canvas.drawRoundRect(
      start.toFloat(),
      (boxStrokeWidth / 2).toFloat(),
      (start + rectWidth).toFloat(),
      height.toFloat() - boxStrokeWidth / 2,
      rectRadius.toFloat(),
      rectRadius.toFloat(),
      strokePaint
    )
  }

  private fun getCode(): String {
    return chars.joinToString("")
  }

  fun setCode(pinCode: String) {
    if (pinCode.length == digitNumber) {
      chars.forEachIndexed { index, _ ->
        chars[index] = pinCode[index]
      }

      onTextChanged()
    }
  }

  private inner class CodeEntryTextWatcher : TextWatcher {

    override fun afterTextChanged(s: Editable?) {
      if (!isBlocked) {
        val string = s?.toString() ?: ""
        for (i in 0 until digitNumber) {
          chars[i] = string.getOrNull(i) ?: EMPTY_SIGN
        }
        onTextChanged()
      }
    }

    override fun beforeTextChanged(
      s: CharSequence?,
      start: Int,
      count: Int,
      after: Int,
    ) {
      /*unused*/
    }

    override fun onTextChanged(
      s: CharSequence?,
      start: Int,
      before: Int,
      count: Int,
    ) {
      /*unused*/
    }
  }
}
