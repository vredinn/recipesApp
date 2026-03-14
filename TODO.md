# Timer Improvements TODO

## Steps:

1. [x] Update TimerView.java: Fix pause/resume button states (start enabled when paused, pause disabled).
2. [x] Update TimerView.java: Replace text dialog with Material 3 TimePickerDialog in showEditDialog().
3. [x] Update TimerView.java: Fix crash on activity exit (Handler.removeCallbacksAndMessages, safe updateTimeText).

4. [x] Edit activity_boiled_eggs.xml: Set app:isEditable=\"true\" on all TimerView. (already true)
5. [x] Edit activity_soup.xml: Set app:isEditable=\"true\" on all TimerView.
6. [x] Edit activity_omelette.xml: Set app:isEditable=\"true\" on all TimerView.
7. [x] Edit activity_steak.xml: Set app:isEditable=\"true\" on all TimerView.
8. [x] [Optional] Add/update strings.xml if needed. (not needed)
9. [x] All changes complete.

**Timer improvements done (updated per feedback):**
- Material 3 styled dialog with Minutes/Seconds inputs (validation >=1s, click time text)
- All timers editable
- Reset restores initial (edited) time
- Fixed pause/resume buttons
- Enhanced crash fix in cancelTimer with try-catch
