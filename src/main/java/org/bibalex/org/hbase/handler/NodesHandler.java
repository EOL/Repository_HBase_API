package org.bibalex.org.hbase.handler;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.*;
import java.lang.reflect.Field;

import org.apache.avro.generic.GenericData;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.PrefixFilter;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.util.Bytes;
import org.bibalex.org.hbase.models.*;

public class NodesHandler {

    private String tableName;
    private HbaseHandler hbaseHandler;
    private Random rand;
    private final int randomGeneratorMax = 10;

    public NodesHandler(HbaseHandler hbaseHandler, String tableName, String columnFamiliesFilePath)
    {
        this.hbaseHandler = hbaseHandler;
        this.rand = new Random();
        this.tableName = tableName;
    }

    private int getNextRandom()
    {
        return this.rand.nextInt(randomGeneratorMax);
    }

    private long getCurrentTimeStamp()
    {
        return System.currentTimeMillis();
    }

    private UUID generateMediaGUID()
    {
        return UUID.randomUUID();
    }

    public boolean addRow(NodeRecord node)
    {
        String keyParts = this.getNextRandom() + "_" + node.getResourceId() +  "_" + node.getGeneratedNodeId();
        Put p = new Put(Bytes.toBytes(keyParts));

        // add Vernacular names of node
        if(node.getVernaculars() != null) {
            byte[] namesColumnFamily = Bytes.toBytes("Names");
            for (VernacularName vn : node.getVernaculars()) {
                String columnQualifier = vn.getName();
                try {
                    byte[] value = NodesHandler.serialize(vn);
                    p.addColumn(namesColumnFamily, Bytes.toBytes(columnQualifier), value);
                } catch (IOException e) {
                    System.out.println("Vernacular Name serialization error");
                    e.printStackTrace();
                }
            }
        }
        // add References of node
        if(node.getReferences() != null) {
            byte[] referencesColumnFamily = Bytes.toBytes("References");
            for (Reference reference : node.getReferences()) {
                String columnQualifier = reference.getReferenceId();
                try {
                    byte[] value = NodesHandler.serialize(reference);
                    p.addColumn(referencesColumnFamily, Bytes.toBytes(columnQualifier), value);
                } catch (IOException e) {
                    System.out.println("Reference serialization error");
                    e.printStackTrace();
                }
            }
        }

        // add media
        // add media of node
        byte[] attributesColumnFamily = Bytes.toBytes("Attributes");
        if(node.getMedia() != null) {
            for (Media md : node.getMedia()) {
                String mediaKeyParts = this.generateMediaGUID() + "";
                Put mediaPut = new Put(Bytes.toBytes(mediaKeyParts));
                String columnQualifier = md.getMediaId() + "_" + md.getType();
                try {
                    byte[] value = NodesHandler.serialize(md);
                    mediaPut.addColumn(attributesColumnFamily, Bytes.toBytes(columnQualifier), value);
                } catch (IOException e) {
                    System.out.println("media serialization error");
                    e.printStackTrace();
                }
                List<Agent> agentsOfMedia = md.getAgents();
                if(agentsOfMedia != null) {
                    byte[] agentsColumnFamily = Bytes.toBytes("Agents");
                    for (Agent agent : agentsOfMedia) {
                        String agentColumnQualifier = agent.getAgentId();
                        try {
                            byte[] value = NodesHandler.serialize(agent);
                            mediaPut.addColumn(agentsColumnFamily, Bytes.toBytes(columnQualifier), value);
                        } catch (IOException e) {
                            System.out.println("agent serialization error");
                            e.printStackTrace();
                        }
                    }
                }
                if(node.getRelation() == null)
                    node.setRelation(new Relation());
                if (node.getRelation().getGuids() == null) {
                    node.getRelation().setGuids(new ArrayList<String>());
                    node.getRelation().getGuids().add(mediaKeyParts);
                } else
                    node.getRelation().getGuids().add(mediaKeyParts);
                hbaseHandler.addRow("Media", mediaPut);
            }
        }


        // add relations of node
        if(node.getRelation() != null) {
            byte[] relationsColumnFamily = Bytes.toBytes("Relations");
            byte[] columnQualifier = Bytes.toBytes("relation");
            try {
                byte[] value = NodesHandler.serialize(node.getRelation());
                p.addColumn(relationsColumnFamily, columnQualifier, value);
            } catch (IOException e) {
                System.out.println("Relation serialization error");
                e.printStackTrace();
            }
        }

        // add traits of node
        byte[] traitsColumnFamily = Bytes.toBytes("Traits");
        // add occurrences
        if(node.getOccurrences() != null) {
            for (Occurrence occurrence : node.getOccurrences()) {
                String occurrenceColumnQualifier = occurrence.getOccurrenceId();
                try {
                    byte[] value = NodesHandler.serialize(occurrence);
                    p.addColumn(traitsColumnFamily, Bytes.toBytes(occurrenceColumnQualifier), value);
                } catch (IOException e) {
                    System.out.println("occurrences serialization error");
                    e.printStackTrace();
                }
            }
        }
        // add measurementOrFacts
        if(node.getMeasurementOrFacts() != null) {
            for (MeasurementOrFact measurementOrFact : node.getMeasurementOrFacts()) {
                String measurementColumnQualifier = measurementOrFact.getOccurrenceId() + "_" +
                        measurementOrFact.getMeasurementType() + "_" +
                        measurementOrFact.getMeasurementValue();
                try {
                    byte[] value = NodesHandler.serialize(measurementOrFact);
                    p.addColumn(traitsColumnFamily, Bytes.toBytes(measurementColumnQualifier), value);
                } catch (IOException e) {
                    System.out.println("measurementOrFacts serialization error");
                    e.printStackTrace();
                }
            }
        }
        // add associations
        if(node.getAssociations() != null) {
            for (Association association : node.getAssociations()) {
                String associationColumnQualifier = association.getOccurrenceId() + "_" +
                        association.getTargetOccurrenceId() + "_" +
                        association.getAssociationType();
                try {
                    byte[] value = NodesHandler.serialize(association);
                    p.addColumn(traitsColumnFamily, Bytes.toBytes(associationColumnQualifier), value);
                } catch (IOException e) {
                    System.out.println("association serialization error");
                    e.printStackTrace();
                }
            }
        }
        if(node.getVernaculars() != null || node.getReferences() != null || node.getRelation() != null || node.getAssociations() != null
                || node.getOccurrences() != null || node.getMeasurementOrFacts() != null)
            return hbaseHandler.addRow(this.tableName, p);
        else
            return false;
    }


    public List<NodeRecord> getNodesOfResource(int resource, String timestamp)
    {
        List<NodeRecord> allNodesOfResource = new ArrayList<NodeRecord>();
        FilterList allFilters = new FilterList(FilterList.Operator.MUST_PASS_ONE);
        for (int i = 0; i < randomGeneratorMax; i++) {
            allFilters.addFilter(new PrefixFilter(Bytes.toBytes(i + "_" + resource)));
        }
        try (ResultScanner results = hbaseHandler.scan(this.tableName, allFilters, timestamp)) {

            for (Result result = results.next(); result != null; result = results.next()) {
                NodeRecord node = new NodeRecord();

                // get scientific name
                String[] rowKeyParts = Bytes.toString(result.getRow()).split("_");
                node.setGeneratedNodeId(rowKeyParts[2]);
                node.setResourceId(Integer.parseInt(rowKeyParts[1]));

                // get Vernaculares of node
                Set<byte[]> vernacularCoulmnQualifiers = result.getFamilyMap(Bytes.toBytes("Names")).keySet();
                for(byte[] i : vernacularCoulmnQualifiers)
                {
                    try {
                        VernacularName vn = (VernacularName) NodesHandler.deserialize(result.getValue(Bytes.toBytes("Names"), i));
                        if(node.getVernaculars() == null)
                        {
                            node.setVernaculars(new ArrayList<VernacularName>());
                            node.getVernaculars().add(vn);
                        }
                        else
                            node.getVernaculars().add(vn);
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                        System.out.println("Vernacular Name serialization Error");
                    }
                }

                // get references of node
                Set<byte[]> referencesCoulmnQualifiers = result.getFamilyMap(Bytes.toBytes("References")).keySet();
                for(byte[] i : referencesCoulmnQualifiers)
                {
                    try {
                        Reference reference = (Reference) NodesHandler.deserialize(result.getValue(Bytes.toBytes("References"), i));
                        if(node.getReferences() == null)
                        {
                            node.setReferences(new ArrayList<Reference>());
                            node.getReferences().add(reference);
                        }
                        else
                            node.getReferences().add(reference);
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                        System.out.println("refernces serialization Error");
                    }
                }

                // get traits of node
                Set<byte[]> traitsCoulmnQualifiers = result.getFamilyMap(Bytes.toBytes("Traits")).keySet();
                for(byte[] i : traitsCoulmnQualifiers)
                {
                    try {
                        Object object = NodesHandler.deserialize(result.getValue(Bytes.toBytes("Traits"), i));
                        if(object instanceof Occurrence) {
                            if(node.getOccurrences() == null)
                            {
                                node.setOccurrences(new ArrayList<Occurrence>());
                                node.getOccurrences().add((Occurrence) object);
                            }
                            else
                                node.getOccurrences().add((Occurrence) object);
                        }
                        else if(object instanceof Association) {
                            if(node.getAssociations() == null)
                            {
                                node.setAssociations(new ArrayList<Association>());
                                node.getAssociations().add((Association) object);
                            }
                            else
                                node.getAssociations().add((Association) object);
                        }
                        else
                        {
                            if(node.getMeasurementOrFacts() == null)
                            {
                                node.setMeasurementOrFacts(new ArrayList<MeasurementOrFact>());
                                node.getMeasurementOrFacts().add((MeasurementOrFact) object);
                            }
                            else
                                node.getMeasurementOrFacts().add((MeasurementOrFact) object);
                        }
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                        System.out.println("refernces serialization Error");
                    }
                }

                // get relations of node
                Set<byte[]> relationsCoulmnQualifiers = result.getFamilyMap(Bytes.toBytes("Relations")).keySet();
                for(byte[] i : relationsCoulmnQualifiers)
                {
                    try {
                        Relation relation = (Relation) NodesHandler.deserialize(result.getValue(Bytes.toBytes("Relations"), i));
                        node.setRelation(relation);
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                        System.out.println("relation serialization Error");
                    }
                }

                // get media and agents of node
                if(node != null && node.getRelation() != null && node.getRelation().getGuids() != null) {
                    node.setMedia(getMediaOfNode(node.getRelation().getGuids()));
                }

                allNodesOfResource.add(node);
            }
            return allNodesOfResource;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<Media> getMediaOfNode(List<String> guids)
    {
        List<Media> media = new ArrayList<Media>();
        FilterList allFilters = new FilterList(FilterList.Operator.MUST_PASS_ONE);
        for (String g : guids) {
            allFilters.addFilter(new PrefixFilter(Bytes.toBytes(g)));
        }
        try (ResultScanner results = hbaseHandler.scan("Media", allFilters, null)) {
            for (Result result = results.next(); result != null; result = results.next()) {
                Set<byte[]> attributesCoulmnQualifiers = result.getFamilyMap(Bytes.toBytes("Attributes")).keySet();
                for(byte[] i : attributesCoulmnQualifiers)
                {
                    try {
                        Media md = (Media) NodesHandler.deserialize(result.getValue(Bytes.toBytes("Attributes"), i));

                        // get agents of a given media
                        Set<byte[]> agentsCoulmnQualifiers = result.getFamilyMap(Bytes.toBytes("Agents")).keySet();
                        for(byte[] j : agentsCoulmnQualifiers)
                        {
                            try {
                                Agent agent = (Agent) NodesHandler.deserialize(result.getValue(Bytes.toBytes("Agents"), j));
                                if(md.getAgents() == null)
                                {
                                    md.setAgents(new ArrayList<Agent>());
                                    md.getAgents().add(agent);
                                }
                                else
                                    md.getAgents().add(agent);
                            } catch (ClassNotFoundException e) {
                                e.printStackTrace();
                                System.out.println("agent deserialization Error");
                            }
                        }
                        media.add(md);
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                        System.out.println("media deserialization Error");
                    }
                }

            }
            return media;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


//    public void addMedia(List<Media> media, List<Agent> agents)
//    {
//        HashMap<String, Agent> agentsMap = new HashMap<String, Agent> ();
//        for (Agent agent : agents) {
//            agentsMap.put(agent.getAgentId(), agent);
//        }
//
//        // add media of node
//        byte[] attributesColumnFamily = Bytes.toBytes("Attributes");
//        for (Media md : media) {
//            String keyParts = this.generateMediaGUID()+"";
//            Put p = new Put(Bytes.toBytes(keyParts));
//            for (Field field: md.getClass().getDeclaredFields())
//            {
//
//                field.setAccessible(true);
//                String columnQualifier = field.getName();
//                try {
//                    String value = (String) field.get(md);
//                    p.addColumn(attributesColumnFamily, Bytes.toBytes(columnQualifier), Bytes.toBytes(value));
//                } catch (IllegalAccessException e) {
//                    System.out.println("read media attributes error for " + columnQualifier);
//                    e.printStackTrace();
//                }
//            }
//
//            byte[] agentsColumnFamily = Bytes.toBytes("Agents");
//            Agent agentOfMedia = agentsMap.get(md.getAgentId());
//            for (Field field: agentOfMedia.getClass().getDeclaredFields())
//            {
//                field.setAccessible(true);
//                String columnQualifier = field.getName();
//                try {
//                    String value = (String) field.get(agentOfMedia);
//                    p.addColumn(agentsColumnFamily, Bytes.toBytes(columnQualifier), Bytes.toBytes(value));
//                } catch (IllegalAccessException e) {
//                    System.out.println("read agent attributes error for " + columnQualifier);
//                    e.printStackTrace();
//                }
//            }
//            hbaseHandler.addRow("Media", p);
//        }
//
//    }

    public static byte[] serialize(Object obj) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(out);
        os.writeObject(obj);
        return out.toByteArray();
    }
    public static Object deserialize(byte[] data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ObjectInputStream is = new ObjectInputStream(in);
        return is.readObject();
    }



    public static void main(String[] args) throws IOException
    {

        HbaseHandler hb = HbaseHandler.getHbaseHandler();
		  hb.dropTable("Nodes");
        hb.dropTable("Media");
		  hb.createTable("Nodes", new String[] { "Names", "References", "Traits", "Relations" } );
        hb.createTable("Media", new String[] { "Attributes", "Agents" } );
//        NodesHandler nodesHandler = new NodesHandler(hb, "Nodes", "nodes_cf.properties");
//        HashMap<String, byte[]> names = new HashMap<String, byte[]>();
////        names.put("v", arr);
////		  names.put("v_n_1", "v_n");
////		  names.put("sc_n_2", "sc_n");
////		  names.put("v_n_2", "v_n");
//
//        HashMap<String, String> refs = new HashMap<String, String>();
//        refs.put("name", "name");
//        refs.put("url", "url");
//
//        HashMap<String, String> traits = new HashMap<String, String>();
//        traits.put("subject", "subject");
//        traits.put("predicate", "predicate");
//
//        HashMap<String, String> rels = new HashMap<String, String>();
//        rels.put("guid", "guid");
//        rels.put("page_id", "page_id");
//
//        HashMap<String, HashMap<String, byte[]> > values = new HashMap<String, HashMap<String, byte[]> >();
//        values.put("Names", names);
////		  values.put("Traits", traits);
////		  values.put("References", refs);
////		  values.put("Relations", rels);
////		  nodesHandler.addRow(1, "tiger", values);
////
////		  String keyParts = "9_1_tiger";
////		  Put p = new Put(Bytes.toBytes(keyParts));
////		  p.addColumn(Bytes.toBytes("Names"), Bytes.toBytes("sc_n_1"), Bytes.toBytes("gh"));
////		  p.addColumn(Bytes.toBytes("References"), Bytes.toBytes("name"), Bytes.toBytes("wa"));
////		  hb.addRow("Nodes", p);
//
////		  String t = "1502361275695";
//        String t = "1502610955093";
////		  System.out.println(nodesHandler.getNodesOfResource(1, null, "tiger").toJSONString());
    }


}