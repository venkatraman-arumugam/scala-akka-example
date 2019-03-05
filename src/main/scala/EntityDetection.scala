
import java.util.Properties

import StanfordProcessor.ProcessDoc
import Writer.{Location, Person}
import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.routing.RoundRobinPool
import edu.stanford.nlp.pipeline.{CoreDocument, CoreEntityMention, StanfordCoreNLP}

import scala.collection.JavaConversions._
import scala.collection.mutable.ListBuffer

object Executors extends App {
  val system: ActorSystem = ActorSystem("NLPProcessing")
  // val inputFile = new File(args(0)+".txt");
  // val  fileBytes = Files.readAllBytes(inputFile.toPath());
  // val text = new String(fileBytes)
  // val texts : Array[String] = text.split("# inputs #")
  
  val props: Properties = new Properties()
  props.put("annotators", "tokenize, ssplit, pos, lemma, ner")
  props.put("ner.useSUTime", "false")
  props.put("threads", "50")


  val inputSentence = """I look forward to giving you a walkthrough of the product at your convenience.We received the following information from your request:Name: Avijit RoyPhone: 01716766360 Email: aroy13@gmail.com Country: Bangladesh Tell us more about what you are looking for: Please expect a call from me till now . to gather your requirements and schedule a demo In the meanwhile, if you need any assistance, just hit reply."""
  val coreDocument = new CoreDocument(inputSentence)

  val pipeline: StanfordCoreNLP = new StanfordCoreNLP(props)


  pipeline.annotate(coreDocument)

  val writer: ActorRef = system.actorOf(Props(new Writer(coreDocument.entityMentions().size())), "writer")
  
  val stanfordProcessorRouter = system.actorOf(
      RoundRobinPool(50).props(Props(new StanfordProcessor(writer))) , "stanfordProcessorRouter"
  )


  coreDocument.entityMentions().foreach{doc =>
    stanfordProcessorRouter ! ProcessDoc(doc)
  }
  
}


object StanfordProcessor {
  final case class ProcessDoc(entity : CoreEntityMention)
}

class StanfordProcessor( writeActor: ActorRef) extends Actor {
  import StanfordProcessor._
  def receive = {
    case ProcessDoc(entity) => {
      val enType = entity.entityType()

      if(enType.equals("CITY") || enType.equals("STATE_OR_PROVINCE")||enType.equals("LOCATION")||enType.equals("COUNTRY")||enType.equals("ORGANIZATION")){
        writeActor ! Location(entity.text())
      }else{
        writeActor ! Person(entity.text())
      }

    }
  }
  
}


object Writer{  
  final case class Location(text: String)
  final case class Person(text: String)
}

class Writer( totalDocCount : Int) extends Actor{
  import Writer._
  var fileRecived = 0
  val location = ListBuffer[String]()
  val person = ListBuffer[String]()
  val startTime = System.currentTimeMillis
  def receive = {
    case Location(text) => {
      location += text

      fileRecived += 1
      
      if(fileRecived == totalDocCount){

        println("Location", location.toList)
        println("Person", person.toList)
        println("Program end time"+ (System.currentTimeMillis-startTime))
        context.system.terminate()
      }
    }

    case Person(text) => {
      person += text

      fileRecived += 1

      if(fileRecived == totalDocCount){
        println("Location", location.toList)
        println("Person", person.toList)
        println("Program end time"+ (System.currentTimeMillis-startTime))
        context.system.terminate()
      }
    }
  }
}