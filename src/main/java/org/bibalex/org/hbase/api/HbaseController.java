package org.bibalex.org.hbase.api;

import org.bibalex.org.hbase.models.NodeRecord;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;
import org.bibalex.org.hbase.handler.*;
import java.util.List;

@RestController
@RequestMapping("/hbase_api")
public class HbaseController {
	
	@RequestMapping(value = "/addHEntry", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public ResponseEntity<NodeRecord> downloadResource(@RequestBody NodeRecord hE) {

		HbaseHandler hb = HbaseHandler.getHbaseHandler();
		NodesHandler nodesHandler = new NodesHandler(hb, "Nodes", "nodes_cf.properties");
		nodesHandler.addRow(hE);

//		hE.setScientificName(hE.getScientificName() + "---1");
		HttpHeaders headers = new HttpHeaders();
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");   
        
		return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(
                        MediaType.parseMediaType("application/json"))
                .body(hE);
	}

	@RequestMapping(value = "/getLatestNodesOfResource/{resourceId}", method = RequestMethod.GET, produces = "application/json")
	public ResponseEntity<List<NodeRecord>> downloadResource(@PathVariable("resourceId") int resourceId) {

		HbaseHandler hb = HbaseHandler.getHbaseHandler();
		NodesHandler nodesHandler = new NodesHandler(hb, "Nodes", "nodes_cf.properties");

		List<NodeRecord> list = nodesHandler.getNodesOfResource(resourceId, null);
//		hE.setScientificName(hE.getScientificName() + "---1");
		HttpHeaders headers = new HttpHeaders();
		headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
		headers.add("Pragma", "no-cache");
		headers.add("Expires", "0");

		return ResponseEntity
				.ok()
				.headers(headers)
				.contentType(
						MediaType.parseMediaType("application/json"))
				.body(list);
	}

	@RequestMapping(value = "/test", method = RequestMethod.GET)
	public ResponseEntity<String> downloadResource() {
		System.out.println("----------------------------x----------------------------");
		return new ResponseEntity(HttpStatus.OK);
	}
}
