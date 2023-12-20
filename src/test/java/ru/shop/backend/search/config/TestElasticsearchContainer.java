package ru.shop.backend.search.config;

import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.utility.DockerImageName;

public class TestElasticsearchContainer extends ElasticsearchContainer {

    private static final String ELASTIC_DOCKER_IMAGE = "elasticsearch:7.17.15";
    private static final String DISCOVERY_TYPE = "discovery.type";
    private static final String XPACK_SECURITY = "xpack.security.enabled";
    public static final String CLUSTER_NAME = "cluster.name";

    public TestElasticsearchContainer() {
        super(DockerImageName.parse(ELASTIC_DOCKER_IMAGE)
                .asCompatibleSubstituteFor("docker.elastic.co/elasticsearch/elasticsearch"));
        addFixedExposedPort(9200, 9200);
        addEnv(DISCOVERY_TYPE, "single-node");
        addEnv(XPACK_SECURITY, Boolean.FALSE.toString());
        addEnv(CLUSTER_NAME, "elasticsearch");
    }

}
