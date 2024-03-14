import com.google.cloud.vertexai.VertexAI
import com.google.cloud.vertexai.generativeai.GenerativeModel

import scala.jdk.CollectionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.Using

@main def run =
  val projectId = sys.env.getOrElse("PROJECT_ID", throw Error("Set the PROJECT_ID env var"))

  val maybeCandidateStream =
    Using(VertexAI(projectId, "us-central1")) { vertexAI =>
      val model = GenerativeModel("gemini-pro", vertexAI)
      model.generateContentStream("Why is the sky blue?")
        .stream()
        .toScala(LazyList)
        .flatMap(_.getCandidatesList.asScala) // combine the candidates into the stream
    }

  // if there was an error, the .get will propagate it
  maybeCandidateStream.get.foreach { candidate =>
    candidate.getContent.getPartsList.asScala.foreach { part =>
      print(part.getText)
    }
  }
