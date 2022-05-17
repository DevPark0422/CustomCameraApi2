package com.devpark.customcameraapi2.sample

import android.util.Size
import java.lang.Long.signum

import java.util.Comparator

/**
 * Compares two `Size`s based on their areas.
 * 영역에 대한 사이즈 비교하는 역할
 */
internal class CompareSizesByArea : Comparator<Size> {

    // We cast here to ensure the multiplications won't overflow
    override fun compare(lhs: Size, rhs: Size) = signum(lhs.width.toLong() * lhs.height - rhs.width.toLong() * rhs.height)

}
