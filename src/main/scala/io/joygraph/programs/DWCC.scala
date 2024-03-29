package io.joygraph.programs

import com.typesafe.config.Config
import io.joygraph.core.program.{HomogeneousVertexProgram, Vertex}

import scala.collection.mutable

class UWCC extends HomogeneousVertexProgram[Long, Long, Unit, Long] {
  /**
    * Load parameters from vertex program
    *
    * @param conf
    */
  override def load(conf: Config): Unit = {

  }//noop

  /**
    * @param v
    * @return true if halting, false if not halting
    */
  override def run(v: Vertex[Long, Long, Unit], messages: Iterable[Long], superStep: Int)(implicit send: (Long, Long) => Unit, sendAll: (Long) => Unit): Boolean = {
    // Weakly connected components algorithm treats a directed graph as undirected, so we create the missing edges
    if (superStep == 0) {
      val minId = if (v.edges.isEmpty) v.id else v.edges.minBy[Long](_.dst).dst
      v.value = math.min(v.id, minId)
      if (minId != v.id) {
        sendAll(v.value)
      }
      true
    }
    else {
      val currentComponent: Long = v.value
      val candidateComponent = messages.min
      if (candidateComponent < currentComponent) {
        v.value = candidateComponent
        sendAll(candidateComponent)
      }
      true
    }
  }

}

class DWCC extends HomogeneousVertexProgram[Long, Long, Unit, Long] {
  /**
    * Load parameters from vertex program
    *
    * @param conf
    */
  override def load(conf: Config): Unit = {

  }//noop

  private[this] val edgeSet = new mutable.HashSet[Long]()
  private[this] val nullRef = null.asInstanceOf[Unit]

  /**
    * @param v
    * @return true if halting, false if not halting
    */
  override def run(v: Vertex[Long, Long, Unit], messages: Iterable[Long], superStep: Int)(implicit send: (Long, Long) => Unit, sendAll: (Long) => Unit): Boolean = {
    // Weakly connected components algorithm treats a directed graph as undirected, so we create the missing edges
    if (superStep == 0) {
      sendAll(v.id)
      false
    }
    else if (superStep == 1) {
      edgeSet.clear()
      v.edges.foreach(existingEdge => edgeSet += existingEdge.dst)
      messages.foreach(m => if(!edgeSet.contains(m)) v.addEdge(m, nullRef))
      val minId = if (v.edges.isEmpty) v.id else v.edges.minBy[Long](_.dst).dst
      v.value = math.min(v.id, minId)
      if (minId != v.id) {
        sendAll(v.value)
      }
      true
    }
    else {
      val currentComponent: Long = v.value
      val candidateComponent = messages.min
      if (candidateComponent < currentComponent) {
        v.value = candidateComponent
        sendAll(candidateComponent)
      }
      true
    }
  }
}
