//
//import akka.actor.{Actor, ActorLogging, ActorSystem, Props, ActorRef}
//
//import scala.io.StdIn
//import edu.stanford.nlp.util.CoreMap
//import scala.collection.mutable.ListBuffer
//import edu.stanford.nlp.pipeline.Annotation
//import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation
//import scala.collection.JavaConversions._
//import akka.actor.ActorSystem
//import akka.routing.RoundRobinPool
//import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation
//import java.io.FileWriter
//import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation
//import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation
//import edu.stanford.nlp.pipeline.StanfordCoreNLP
//import java.util.Properties
//import java.nio.file.Files
//import java.io.File
//import FileReader.ReadFile
//import Writer.WriteToFile
//
//object Executors extends App {
//  val system: ActorSystem = ActorSystem("NLPProcessing")
//  // val inputFile = new File(args(0)+".txt");
//  // val  fileBytes = Files.readAllBytes(inputFile.toPath());
//  // val text = new String(fileBytes)
//  // val texts : Array[String] = text.split("# inputs #")
//
//  val props: Properties = new Properties()
//  props.put("annotators", "tokenize, ssplit, pos, lemma")
//  props.put("threads", "50")
//
//  def getRecursiveListOfFiles(dir: File): Array[File] = {
//    val these = dir.listFiles
//      these ++ these.filter(_.isDirectory).flatMap(getRecursiveListOfFiles)
//  }
//
//  val allFiles = getRecursiveListOfFiles(new File("/Users/venkat-5179/Desktop/article/"))
//  val writeTo = "/Users/venkat-5179/Desktop/article_processed/"
//  val pipeline: StanfordCoreNLP = new StanfordCoreNLP(props)
//  val dir = new File(writeTo)
//
//  if(!dir.exists()){
//    dir.mkdir
//  }
//
//
//  val writer: ActorRef = system.actorOf(Props(new Writer(writeTo, allFiles.size)), "writer")
//
//  val stanfordProcessorRouter = system.actorOf(
//      RoundRobinPool(50).props(Props(new StanfordProcessor(pipeline, writer))) , "stanfordProcessorRouter"
//  )
//
//  val fileReader = system.actorOf(Props(new FileReader(stanfordProcessorRouter)), "fileReader")
//
//  allFiles.foreach{file =>
//    fileReader ! ReadFile(file)
//  }
//
//}
//
//object FileReader{
//  final case class ReadFile(file : File)
//}
//
//class FileReader(stanfordProcessor : ActorRef) extends Actor {
//  import FileReader._
//  import StanfordProcessor._
//  def receive = {
//    case ReadFile(file) => {
//      val source = scala.io.Source.fromFile(file.getAbsolutePath())
//      val lines = try source.getLines mkString "\n" finally source.close()
//      stanfordProcessor ! ProcessDoc(file.getName(), lines)
//    }
//  }
//}
//
//object StanfordProcessor {
//  final case class ProcessDoc(fileName: String, doc: String)
//}
//
//class StanfordProcessor(pipeline: StanfordCoreNLP, writeActor: ActorRef) extends Actor {
//  import StanfordProcessor._
//  def receive = {
//    case ProcessDoc(fileName, doc) => {
//      val document: Annotation = new Annotation(doc)
//  // run all Annotators on this text
//      pipeline.annotate(document)
//  // a CoreMap is essentially a Map that uses class objects as keys and has values with custom types
//      val sentences: java.util.List[CoreMap] = document.get(classOf[SentencesAnnotation])
//      val sen_tokens = ListBuffer[String]()
//      val sen_lemmas = ListBuffer[String]()
//      for (sentence <- sentences) {
//  // a CoreLabel is a CoreMap with additional token-specific methods
//        val tokens = ListBuffer[String]()
//          val lemmas = ListBuffer[String]()
//        for (token <- sentence.get(classOf[TokensAnnotation])) {
//  // this is the text of the token
//          val word: String = token.get(classOf[TextAnnotation])
//          tokens += word
//  // this is the NER label of the token
//          val lemma: String = token.get(classOf[LemmaAnnotation])
//          lemmas += lemma
//  //        println("word: " + word  + " Lemma:" + lemma)
//        }
//        sen_tokens += tokens.toList.mkString("#token#")
//        sen_lemmas += lemmas.toList.mkString("#lemmatoken#")
//      }
//
//      writeActor ! WriteToFile(fileName,
//          sen_tokens.toList.mkString("# Sentence") + "# tokens & lemmas #" +sen_lemmas.toList.mkString("# Sentence")+"# docs #"
//          )
//    }
//  }
//
//}
//
//
//object Writer{
//  final case class WriteToFile(fileName: String, processedDoc: String)
//}
//
//class Writer(writeTo : String, totalDocCount : Int) extends Actor{
//  import Writer._
//  var fileRecived = 0
//  def receive = {
//    case WriteToFile(fileName, processedDoc) => {
//      val  writer = new FileWriter(writeTo+fileName);
//      writer.write(processedDoc)
//      writer.close()
//      fileRecived += 1
//      if(fileRecived % 1000 == 0){
//        println("Proccessed No of Files"+fileRecived)
//      }
//
//      if(fileRecived == totalDocCount){
//        context.system.terminate()
//      }
//    }
//  }
//}