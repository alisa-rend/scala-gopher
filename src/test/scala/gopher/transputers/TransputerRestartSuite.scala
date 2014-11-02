package gopher.transputers

import gopher._
import gopher.channels._
import gopher.tags._
import org.scalatest._
import scala.concurrent._
import scala.concurrent.duration._
import akka.actor._

class MyException extends RuntimeException("AAA")

trait BingoWithRecover extends SelectTransputer 
{

  val inX = InPort[Int](1)
  val inY = InPort[Int](1)
  val out = OutPort[Boolean](false)
  val fin = OutPort[Boolean](false)

  var exReaction: SupervisorStrategy.Directive = SupervisorStrategy.Restart
  var throwAlways: Boolean = false

  override def copyState(prev: Transputer):Unit =
  {
    val bingoPrev = prev.asInstanceOf[BingoWithRecover]
    exReaction = bingoPrev.exReaction
    throwAlways = bingoPrev.throwAlways
  }


  recover {
       case ex: ChannelClosedException => 
                                     SupervisorStrategy.Stop
       case ex: MyException => 
                                     SupervisorStrategy.Restart
  }


}



class TransputerRestartSuite extends FunSuite
{

  test("bingo copy call") {
    // val bingo = gopherApi.makeTransputer[BingoWithRecover]
    val bingo = { def factory(): BingoWithRecover = new BingoWithRecover {
                        def api = gopherApi
                        def recoverFactory = factory
                     }
      val retval = factory()
      retval
     }

     val bingo1 = bingo.recoverFactory()
     bingo1.copyPorts(bingo)
  }


  def gopherApi = CommonTestObjects.gopherApi

}
