package kamon.jmx.collector

import akka.actor.{ActorSystem, Props}
import com.typesafe.config.ConfigFactory
import kamon.jmx.collector.JmxMetricCollectorActor.CollectMetrics

import scala.concurrent.ExecutionContext

object KamonJmxMetricCollector extends Configuration {
  def apply()(implicit system: ActorSystem): Unit = {

    implicit val executionContext: ExecutionContext = system.dispatcher

    lazy val configuration = ConfigFactory.load()

    val metricConfiguration = parseConfiguration(configuration)

    val (jmxMbeansAndAttributes, jmxMbeansAndMetricNames, configWithObjectNames, errorsFromConfigWithObjectNames) =
      MetricCollector.getJmxMbeanEntities(metricConfiguration.metrics)

    val jmxMetricCollectorActor = system.actorOf(Props(new JmxMetricCollectorActor(
      metricConfiguration.metrics, jmxMbeansAndAttributes, jmxMbeansAndMetricNames, configWithObjectNames, errorsFromConfigWithObjectNames)
    ))

    system.scheduler.schedule(metricConfiguration.initialDelay, metricConfiguration.checkInterval, jmxMetricCollectorActor, CollectMetrics)
  }
}
