# Merritt Inventory Service

This microservice is part of the [Merritt Preservation System](https://github.com/CDLUC3/mrt-doc).

## Purpose

This microservice records information about new files and new objects that have been ingested into the Merritt Preservation System.
Information is recorded into the Merritt Inventory Database (MySQL).

The work performed by this service is driven from a Zookeeper Queue.  
This service also provides an API to resolve local identifiers submitted with Merritt content.

## Original System Specifications
- [Merritt Inventory Service](https://github.com/CDLUC3/mrt-doc/blob/main/doc/Merritt-inventory-latest.pdf)

## Component Diagram

```mermaid
%%{init: {'theme': 'neutral', 'securityLevel': 'loose', 'themeVariables': {'fontFamily': 'arial'}}}%%
graph TD
  ING(Ingest)
  click ING href "https://github.com/CDLUC3/mrt-ingest" "source code"
  ST(Storage)
  click ST href "https://github.com/CDLUC3/mrt-store" "source code"
  ZOOINV>Zookeeper Inventory]
  click ZOOINV href "https://github.com/CDLUC3/mrt-zoo" "source code"
  INV(Inventory)
  click INV href "https://github.com/CDLUC3/mrt-inventory" "source code"
  RDS[(Inventory DB)]

  subgraph flowchart
    ING --> |Local Id Request| INV
    ZOOINV --> |acquire task| INV
    INV --> |retrieve manifest| ST
    ST --> |manifest file| INV
    INV --> RDS
  end
  
  style RDS fill:#F68D2F
  style ZOOINV fill:cyan
  style INV stroke:red,stroke-width:4px
```

## Dependencies

This code depends on the following Merritt Libraries.
- [Merritt Core Library](https://github.com/CDLUC3/mrt-core2)
- [CDL Zookeeper Library](https://github.com/CDLUC3/cdl-zk-queue)
- [Merritt Inventory Zookeeper Library](https://github.com/CDLUC3/mrt-zoo)

## For external audiences
This code is not intended to be run apart from the Merritt Preservation System.

See [Merritt Docker](https://github.com/CDLUC3/merritt-docker) for a description of how to build a test instnce of Merritt.

## Build instructions
This code is deployed as a war file. The war file is built on a Jenkins server.

## Test instructions

## Internal Links

### Deployment and Operations at CDL

https://github.com/CDLUC3/mrt-doc-private/blob/main/uc3-mrt-inventory.md
