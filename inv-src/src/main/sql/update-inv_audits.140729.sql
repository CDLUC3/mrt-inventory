UPDATE inv_audits SET url = REPLACE(url, '/7003/', '/9001/') 
WHERE url LIKE '%/7003/%'
and inv_audits.inv_node_id=7;