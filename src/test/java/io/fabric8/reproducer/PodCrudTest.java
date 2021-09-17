package io.fabric8.reproducer;

import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.server.mock.EnableKubernetesMockClient;
import io.fabric8.kubernetes.client.server.mock.KubernetesMockServer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnableKubernetesMockClient(crud = true)
class PodCrudTest {
    KubernetesMockServer server;
    KubernetesClient client;

    @Test
    void testDeleteActuallyRemovesPods() {
        // Given
        Pod pod1 = new PodBuilder().withNewMetadata().withName("pod1").addToLabels("testKey", "testValue").endMetadata().build();

        // When
        client.pods().create(pod1);
        client.pods().delete();

        // Then
        assertNull(client.pods().withName("pod1").get());
    }

    @Test
    void testDeleteAllPodsInDifferentNamespaces() {
        // Given
        Pod pod1 = new PodBuilder().withNewMetadata().withName("p1").addToLabels("testKey", "testValue").endMetadata().build();
        Pod pod2 = new PodBuilder().withNewMetadata().withName("p2").addToLabels("testKey", "testValue").endMetadata().build();
        Pod pod3 = new PodBuilder().withNewMetadata().withName("p3").addToLabels("testKey", "testValue").endMetadata().build();
        client.pods().inNamespace("ns1").create(pod1);
        client.pods().inNamespace("ns2").create(pod2);
        client.pods().inNamespace("ns3").create(pod3);

        // When
        client.pods().delete();
        PodList ns1Pods = client.pods().inNamespace("ns1").list();
        PodList ns2Pods = client.pods().inNamespace("ns2").list();
        PodList ns3Pods = client.pods().inNamespace("ns3").list();

        // Then
        ns1Pods.getItems().stream().map(Pod::getMetadata).map(ObjectMeta::getName).forEach(System.out::println);
        ns2Pods.getItems().stream().map(Pod::getMetadata).map(ObjectMeta::getName).forEach(System.out::println);
        ns3Pods.getItems().stream().map(Pod::getMetadata).map(ObjectMeta::getName).forEach(System.out::println);
        assertTrue(ns1Pods.getItems().isEmpty());
        assertTrue(ns2Pods.getItems().isEmpty());
        assertTrue(ns3Pods.getItems().isEmpty());
    }

    @Test
    void testDeletePodsInEachNamespaceInDifferentNamespaces() {
        // Given
        Pod pod1 = new PodBuilder().withNewMetadata().withName("p1").addToLabels("testKey", "testValue").endMetadata().build();
        Pod pod2 = new PodBuilder().withNewMetadata().withName("p2").addToLabels("testKey", "testValue").endMetadata().build();
        Pod pod3 = new PodBuilder().withNewMetadata().withName("p3").addToLabels("testKey", "testValue").endMetadata().build();
        client.pods().inNamespace("ns1").create(pod1);
        client.pods().inNamespace("ns2").create(pod2);
        client.pods().inNamespace("ns3").create(pod3);

        // When
        client.pods().inNamespace("ns1").delete();
        client.pods().inNamespace("ns2").delete();
        client.pods().inNamespace("ns3").delete();
        PodList ns1Pods = client.pods().inNamespace("ns1").list();
        PodList ns2Pods = client.pods().inNamespace("ns2").list();
        PodList ns3Pods = client.pods().inNamespace("ns3").list();

        // Then
        assertTrue(ns1Pods.getItems().isEmpty());
        assertTrue(ns2Pods.getItems().isEmpty());
        assertTrue(ns3Pods.getItems().isEmpty());
    }
}
