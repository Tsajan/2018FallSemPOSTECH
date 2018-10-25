/**
  * Don't import another package besides below packages
  */
import org.apache.spark.SparkContext
import org.apache.spark.SparkConf
import org.apache.spark.rdd.RDD
import scala.collection.mutable.Map
import math._;

object Kmeans {
  /**
    * This is main function
    * You can create more functions as you want
    */
  def computeCentroids(d: Array[Array[Double]]) = {
    var x:Double = 0
    var y:Double = 0
    var z:Double = 0
    var size:Double = d.size
    for(i <- 0 to d.length-1) {
      x += d(i)(0);
      y += d(i)(1);
      z += d(i)(2);
    }
    Array((x/size), (y/size), (z/size));
  }

  def euclideandist(p1: Array[Double], p2:Array[Double]) = {
    sqrt((p1 zip p2).map { case (x,y) => pow(y -x, 2)}.sum)
  }
  // this function will assign a cluster number corresponding
  // to the key value of the centroid map object depending on the least difference in euclidean distance
  def groupClusters(centroids: Map[Int, Array[Double]]) = (dataPoint: Array[Double]) => {
    var clusterNum = 0
    //initially assign an arbitrary maximum value
    var minDist = Double.MaxValue
    for((cluster, position) <- centroids) {
      val dist = euclideandist(dataPoint, position)
      if(dist < minDist) {
        //initially dist is always less than minDist
        //so the first centroid key will be cluster number
        //but that will change as we loop over each of the centroid points
        clusterNum = cluster
        minDist = dist
      }
    }
    //return the given dataPoint with cluster number assigned
    (clusterNum, dataPoint)
  }

  def main(args: Array[String]) {
    if (args.length < 3) {
      System.err.println("Usage: KMeans <input_file> <output_file> <mode> <k>")
      System.exit(1)
    }

    /**
      * Don't modify following initialization phase
      */
    val sparkConf = new SparkConf().setAppName("KMeans").set("spark.cores.max", "3")
    val sc = new SparkContext(sparkConf)
    // val lines is base RDD
    val lines = sc.textFile(args(0))
    val mode = args(2).toInt
    /**
      * From here, you can modify codes.
      * you can use given data structure, or another data type and RDD operation
      * you must utilize more than 5 types of RDD operations
      */
    //remove duplicate entries
    var distData = lines.distinct().map(_.split(",").map(_.toDouble))
    //put the distData into cache
    distData = distData.cache();
    //count the number of entries after getting distinct rows
    var N = distData.count().toInt

    var K: Int = 0
    var centroids: Map[Int, Array[Double]] = Map();
    //output will be the data output by our program
    // key will contain the centroid number and Array of Double will contain the positional components
    //var output: Map[Int, List[Array[Double]]] = Map();
    // Set initial centroids
    if (mode == 0) {
      // randomly sample K data points
      K = args(3).toInt
      // centroids = ...
      //get a random 3 sample from the given input data
      //threeSamples will be of an Array of type String, where each string is a line/record
      val threeSamples = distData.takeSample(false, K, System.nanoTime.toInt);
      for( i <- 0 to threeSamples.length-1 ) {
        centroids += (i+1) -> threeSamples(i);
      }
    }

    else {
      // user-defined centroids
      // you can use another built-in data type besides Map
      centroids = Map(1 -> Array(5, 1.2, -0.8), 2 -> Array(-3.2, -1.1, 3.0), 3 -> Array(-2.1, 5.1, 1.1))
      K = centroids.size
    }
    /**
      * Don't change termination condition
      * sum of moved centroid distances
      */
    var diff:Array[Double] = new Array[Double](3)
    //create an empty RDD
    var myRDD: RDD[(Int, Array[Double])] = sc.emptyRDD
    var change : Double = 100
    while(change > 0.001) {
      //map this RDD with cluster number and the datapoints using group cluster functions
      myRDD = distData.map(groupClusters(centroids))
      //create 3 array of arrays to store nearby each centroid points
      var pointSet1:Array[Array[Double]] = Array()
      var pointSet2:Array[Array[Double]] = Array()
      var pointSet3:Array[Array[Double]] = Array()

      //copy previous set of centroids to prevCentroids
      var prevCentroids = centroids;
      for(i <- 1 to N) {
        var tmp: Array[Array[Double]] = Array()
        if(i == 1) {
          tmp = distData.take(i)
        }
        else {
          //to take the ith row only, take the first i rows and drop the previous i-1 rows
          tmp = distData.take(i).drop(i-1);
        }
        var rec:Array[Double] = tmp(0)
        //compute euclidean distance for the single data point with each centroid point
        for(j <- 1 to diff.length) {
          diff(j-1) = euclideandist(prevCentroids(j), rec);
        }
        var pos = diff.indexOf(diff.min)+1; //because we require 1,2,3 as centroid num and not 0,1,2
        //output += pos -> :+rec;
        if(pos == 1) {
          pointSet1 :+ rec
        }
        else if(pos == 2) {
          pointSet2 :+ rec
        }
        else if(pos == 3) {
          pointSet3 :+ rec
        }
      }
      var c1 = computeCentroids(pointSet1)
      var c2 = computeCentroids(pointSet2)
      var c3 = computeCentroids(pointSet3)
      change = euclideandist(c1, prevCentroids(1)) + euclideandist(c2, prevCentroids(2)) + euclideandist(c3, prevCentroids(3))
      //update the centroid values
      centroids = Map(1 -> c1, 2 -> c2, 3 -> c3)
    }

    //write to text file once the loop of change is complete
    myRDD.map(x => x._1.toString + "\t" + x._2.mkString(",")).sortBy(x => x).saveAsTextFile(args(1))

  }
}