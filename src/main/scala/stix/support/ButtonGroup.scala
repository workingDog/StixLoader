package stix.support

import com.jfoenix.controls.JFXButton
import scala.collection.mutable


class ButtonGroup(fromGroup: Boolean) {

  val fromColor = "-fx-background-color: #2980B9; -fx-text-fill: white; "
  val toColor = "-fx-background-color: #00AA8D; -fx-text-fill: white; "

  private val gColor = if (fromGroup) fromColor else toColor

  private val onStyle = "-fx-background-color: pink; " + "-fx-background-radius: 5em; "
  private val offStyle = gColor + "-fx-background-radius: 5em; "

  class Entry(var k: Boolean, val b: JFXButton)

  val entryList = mutable.ListBuffer[Entry]()

  /**
    * select thisButton and "clear" all others
    *
    * @param thisButton
    * @param userData
    */
  def setSelected(thisButton: JFXButton, userData: AnyRef): Unit = {
    entryList.foreach(e =>
      if (e.b == thisButton) {
        e.k = !e.k
        e.b.setStyle(if (e.k) onStyle else offStyle)
        e.b.setUserData(userData)
      }
      else {
        e.b.setStyle(offStyle)
        e.k = false
        e.b.setUserData(null)
      })
  }

  def clearAllSelection(): Unit = {
    entryList.foreach(e => {
      e.b.setStyle(offStyle)
      e.k = false
      e.b.setUserData(null)
    })
  }

  def isSelected(thisButton: JFXButton): Boolean = {
    val entry = entryList.find(e => e.b == thisButton)
    if (entry.isEmpty) false else entry.get.k
  }

  def getSelected(): Option[JFXButton] = {
    entryList.find(e => e.k).map(e => e.b)
  }

  def add(thisButton: JFXButton): Unit = {
    entryList += new Entry(false, thisButton)
  }

  def add(selected: Boolean, thisButton: JFXButton): Unit = {
    entryList += new Entry(selected, thisButton)
  }

}
