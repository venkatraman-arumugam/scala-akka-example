
// import akka.actor.{Actor, ActorLogging, ActorSystem, Props, ActorRef}

// import scala.io.StdIn
// import edu.stanford.nlp.util.CoreMap
// import scala.collection.mutable.ListBuffer
// import edu.stanford.nlp.pipeline.Annotation
// import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation
// import scala.collection.JavaConversions._
// import akka.actor.ActorSystem
// import akka.routing.RoundRobinPool
// import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation
// import java.io.FileWriter
// import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation
// import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation
// import edu.stanford.nlp.pipeline.StanfordCoreNLP
// import java.util.Properties
// import java.nio.file.Files
// import java.io.File
// import StanfordProcessor.ProcessDoc
// import Writer.WriteToFile

// object Executor extends App {
//   val system: ActorSystem = ActorSystem("NLPProcessing")
//   val inputFile = new File(args(0)+".txt");
//   val  fileBytes = Files.readAllBytes(inputFile.toPath());
//   val text = new String(fileBytes)
//   val texts : Array[String] = text.split("# inputs #")
  
//   val props: Properties = new Properties()
//   props.put("annotators", "tokenize, ssplit, pos, lemma")
//   props.put("threads", "20")

//   val pipeline: StanfordCoreNLP = new StanfordCoreNLP(props)
//   val writer: ActorRef = system.actorOf(Props(new Writer(args(0), texts.length)), "writer")
  
  
//   val stanfordProcessorRouter = system.actorOf(
//       RoundRobinPool(20).props(Props(new StanfordProcessor(pipeline, writer))) , "stanfordProcessorRouter"
//   )
  
//   texts.foreach{doc => 
//     stanfordProcessorRouter ! ProcessDoc(doc)
//   }
  
// }

// object StanfordProcessor {
//   final case class ProcessDoc(doc: String)
// }

// class StanfordProcessor(pipeline: StanfordCoreNLP, writeActor: ActorRef) extends Actor {
//   import StanfordProcessor._
//   def receive = {
//     case ProcessDoc(doc) => {
//       val document: Annotation = new Annotation(doc)
//   // run all Annotators on this text
//       pipeline.annotate(document)
//   // a CoreMap is essentially a Map that uses class objects as keys and has values with custom types
//       val sentences: java.util.List[CoreMap] = document.get(classOf[SentencesAnnotation])
//       val sen_tokens = ListBuffer[String]()
//       val sen_lemmas = ListBuffer[String]()
//       for (sentence <- sentences) {
//   // a CoreLabel is a CoreMap with additional token-specific methods
//         val tokens = ListBuffer[String]()
//           val lemmas = ListBuffer[String]()
//         for (token <- sentence.get(classOf[TokensAnnotation])) {
//   // this is the text of the token
//           val word: String = token.get(classOf[TextAnnotation])
//           tokens += word
//   // this is the NER label of the token
//           val lemma: String = token.get(classOf[LemmaAnnotation])
//           lemmas += lemma
//   //        println("word: " + word  + " Lemma:" + lemma)
//         }
//         sen_tokens += tokens.toList.mkString("#token#")
//         sen_lemmas += lemmas.toList.mkString("#lemmatoken#")
//       }
      
//       writeActor ! WriteToFile(
//           sen_tokens.toList.mkString("# Sentence") + "# tokens & lemmas #" +sen_lemmas.toList.mkString("# Sentence")+"# docs #"
//           )
//     }
//   }
  
// }


// object Writer{  
//   final case class WriteToFile(processedDoc: String)
// }

// class Writer(file_name : String, totalDocCount : Int) extends Actor{
//   import Writer._
//   val  writer = new FileWriter(file_name+"_output.txt"); 
//   var fileRecived = 0
//   def receive = {
//     case WriteToFile(processedDoc) => {
//       writer.write(processedDoc)
//       fileRecived += 1
//       if(fileRecived % 100 == 0){
//         println("Proccessed No of Files"+fileRecived)
//       }
      
//       if(fileRecived == totalDocCount){
//         writer.close()
//         context.system.terminate()
//       }
//     }
//   }
// }