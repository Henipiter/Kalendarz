package com.example.kaledarz.helpers

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import com.example.kaledarz.DTO.Status
import com.example.kaledarz.R

class DrawableHelper {
    companion object {
        @ColorRes
        fun getColorByNoteCyclicType(isRegular: Boolean): Int {
            return if (isRegular) {
                R.color.done
            } else {
                R.color.white
            }
        }

        @DrawableRes
        fun getDrawableByStatus(statuses: Set<Status>, isRegular: Boolean): Int? {
            if (isRegular) {
                if (statuses.size == 4) {
                    return R.drawable.image_round_all_bonus
                }
                if (statuses.size == 3) {
                    if (!statuses.contains(Status.DONE)) {
                        return R.drawable.event_three_undone_past_future_regular
                    }
                    if (!statuses.contains(Status.UNDONE)) {
                        return R.drawable.event_three_done_past_future_regular
                    }
                    if (!statuses.contains(Status.PAST)) {
                        return R.drawable.event_three_done_undone_future_regular
                    }
                    if (!statuses.contains(Status.FUTURE)) {
                        return R.drawable.event_three_done_undone_past_regular
                    }
                }
                if (statuses.size == 2) {
                    if (statuses.contains(Status.DONE) && statuses.contains(Status.UNDONE)) {
                        return R.drawable.event_two_done_undone_regular
                    }
                    if (statuses.contains(Status.DONE) && statuses.contains(Status.PAST)) {
                        return R.drawable.event_two_done_past_regular
                    }
                    if (statuses.contains(Status.DONE) && statuses.contains(Status.FUTURE)) {
                        return R.drawable.event_two_done_future_regular
                    }
                    if (statuses.contains(Status.UNDONE) && statuses.contains(Status.PAST)) {
                        return R.drawable.event_two_undone_past_regular
                    }
                    if (statuses.contains(Status.UNDONE) && statuses.contains(Status.FUTURE)) {
                        return R.drawable.event_two_undone_future_regular
                    }
                    if (statuses.contains(Status.PAST) && statuses.contains(Status.FUTURE)) {
                        return R.drawable.event_two_past_future_regular
                    }
                }
                if (statuses.size == 1) {
                    if (statuses.contains(Status.DONE)) {
                        return R.drawable.event_one_done_regular
                    }
                    if (statuses.contains(Status.UNDONE)) {
                        return R.drawable.event_one_undone_regular
                    }
                    if (statuses.contains(Status.PAST)) {
                        return R.drawable.event_one_past_regular
                    }
                    if (statuses.contains(Status.FUTURE)) {
                        return R.drawable.event_one_future_regular
                    }
                }
                return R.drawable.event_zero_regular
            }




            if (statuses.size == 4) {
                return R.drawable.image_round_all
            }
            if (statuses.size == 3) {
                if (!statuses.contains(Status.DONE)) {
                    return R.drawable.event_three_undone_past_future
                }
                if (!statuses.contains(Status.UNDONE)) {
                    return R.drawable.event_three_done_past_future
                }
                if (!statuses.contains(Status.PAST)) {
                    return R.drawable.event_three_done_undone_future
                }
                if (!statuses.contains(Status.FUTURE)) {
                    return R.drawable.event_three_done_undone_past
                }
            }
            if (statuses.size == 2) {
                if (statuses.contains(Status.DONE) && statuses.contains(Status.UNDONE)) {
                    return R.drawable.event_two_done_undone
                }
                if (statuses.contains(Status.DONE) && statuses.contains(Status.PAST)) {
                    return R.drawable.event_two_done_past
                }
                if (statuses.contains(Status.DONE) && statuses.contains(Status.FUTURE)) {
                    return R.drawable.event_two_done_future
                }
                if (statuses.contains(Status.UNDONE) && statuses.contains(Status.PAST)) {
                    return R.drawable.event_two_undone_past
                }
                if (statuses.contains(Status.UNDONE) && statuses.contains(Status.FUTURE)) {
                    return R.drawable.event_two_undone_future
                }
                if (statuses.contains(Status.PAST) && statuses.contains(Status.FUTURE)) {
                    return R.drawable.event_two_past_future
                }
            }
            if (statuses.size == 1) {
                if (statuses.contains(Status.DONE)) {
                    return R.drawable.event_one_done
                }
                if (statuses.contains(Status.UNDONE)) {
                    return R.drawable.event_one_undone
                }
                if (statuses.contains(Status.PAST)) {
                    return R.drawable.event_one_past
                }
                if (statuses.contains(Status.FUTURE)) {
                    return R.drawable.event_one_future
                }
            }
            return null
        }
    }
}