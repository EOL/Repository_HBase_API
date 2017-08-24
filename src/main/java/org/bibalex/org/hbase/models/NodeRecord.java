package org.bibalex.org.hbase.models;

import java.io.Serializable;
import java.util.List;

/*
This class will include the needed objects and attributes
 */
public class NodeRecord implements Serializable {
    String scientificName;
    int resourceId;

    Relation relation;

    List<VernacularName> vernaculars;
    List<Reference> references;
    List<Occurrence> occurrences;
    List<Association> associations;
    List<MeasurementOrFact> measurementOrFacts;
    List<Media> media;
    List<Agent> agents;

    public Relation getRelation() {
        return relation;
    }

    public void setRelation(Relation relation) {
        this.relation = relation;
    }

    public List<Association> getAssociations() {
        return associations;
    }

    public void setAssociations(List<Association> associations) {
        this.associations = associations;
    }

    public List<MeasurementOrFact> getMeasurementOrFacts() {
        return measurementOrFacts;
    }

    public void setMeasurementOrFacts(List<MeasurementOrFact> measurementOrFacts) {
        this.measurementOrFacts = measurementOrFacts;
    }

    public List<Agent> getAgents() {
        return agents;
    }

    public void setAgents(List<Agent> agents) {
        this.agents = agents;
    }

    public int getResourceId() {
        return resourceId;
    }

    public void setResourceId(int resourceId) {
        this.resourceId = resourceId;
    }

    public List<Media> getMedia() {
        return media;
    }

    public void setMedia(List<Media> media) {
        this.media = media;
    }

    public List<Occurrence> getOccurrences() {
        return occurrences;
    }

    public void setOccurrences(List<Occurrence> occurrences) {
        this.occurrences = occurrences;
    }

    public List<Reference> getReferences() {
        return references;
    }

    public void setReferences(List<Reference> references) {
        this.references = references;
    }

    public String getScientificName() {

        return scientificName;
    }

    public void setScientificName(String scientificName) {
        this.scientificName = scientificName;
    }

    public List<VernacularName> getVernaculars() {
        return vernaculars;
    }

    public void setVernaculars(List<VernacularName> vernaculars) {
        this.vernaculars = vernaculars;
    }

}
