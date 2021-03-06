package gopher.channels

import gopher._
import scala.annotation._
import scala.concurrent._
import scala.util._
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicInteger
import async.Async._



class DuppedInput[A](origin:Input[A])
{

  def pair = (sink1, sink2)

  val sink1 = api.makeChannel[A]()
  val sink2 = api.makeChannel[A]()

  // can't use macroses, so unroll by hands.
  private val selector = api.select.forever;
  selector.readingWithFlowTerminationAsync(origin, 
    (ec:ExecutionContext, ft: FlowTermination[Unit], a: A) => async{ 
        val f1 = sink1.awrite(a)
        val f2 = sink2.awrite(a)
        await(f1)
        await(f2)
        ();
    }(ec) )
  selector.go.onFailure{
    case ex: ChannelClosedException =>
                   sink1.close()
                   sink2.close()
  }

  def api = origin.api
  private implicit def ec:ExecutionContext = api.executionContext

  private var closed = false


}
