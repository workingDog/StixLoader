package stix.support

import com.jfoenix.controls.JFXButton
import stix.info.{InfoMessage, NoInfo}

import scala.collection.mutable


/**
  * a group system for a set of JFXButtons
  * @param fromGroup
  */
class ButtonGroup(fromGroup: Boolean) {

  val fromColor = "-fx-background-color: #2980B9; -fx-text-fill: white; "
  val toColor = "-fx-background-color: #00AA8D; -fx-text-fill: white; "

  private val theColor = if (fromGroup) fromColor else toColor

  private val onStyle = "-fx-background-color: pink; " + "-fx-background-radius: 5em; "
  private val offStyle = theColor + "-fx-background-radius: 5em; "

  class Entry(var k: Boolean, val b: JFXButton)

  val entryList = mutable.ListBuffer[Entry]()

  /**
    * select thisButton, set the userData and "clear" all others
    *
    * @param thisButton
    * @param userData
    */
  def setSelected(thisButton: JFXButton, userData: InfoMessage): Unit = {
    entryList.foreach(e =>
      if (e.b == thisButton) {
        e.k = !e.k
        e.b.setStyle(if (e.k) onStyle else offStyle)
        e.b.setUserData(userData)
      }
      else {
        e.b.setStyle(offStyle)
        e.k = false
        e.b.setUserData(NoInfo())
      })
  }

  def clearAllSelection(): Unit = {
    entryList.foreach(e => {
      e.b.setStyle(offStyle)
      e.k = false
      e.b.setUserData(NoInfo())
    })
  }

  def isSelected(thisButton: JFXButton): Boolean = entryList.find(e => e.b == thisButton).exists(_.k)

  def getSelected(): Option[JFXButton] = entryList.find(_.k).map(_.b)

  def add(thisButton: JFXButton): Unit = entryList += new Entry(false, thisButton)

  def add(thisButton: JFXButton, selected: Boolean): Unit = entryList += new Entry(selected, thisButton)

}
