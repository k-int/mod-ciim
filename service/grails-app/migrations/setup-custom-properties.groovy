databaseChangeLog = {
  changeSet(author: "efreestone (manual)", id: "202012101335-001") {
    createTable(tableName: "custom_property") {
      column(autoIncrement: "true", name: "id", type: "BIGINT") {
          constraints(primaryKey: "true", primaryKeyName: "custom_propertyPK")
      }

      column(name: "version", type: "BIGINT") {
          constraints(nullable: "false")
      }

      column(name: "definition_id", type: "VARCHAR(36)")

      column(name: "parent_id", type: "BIGINT")
    }
  }

  changeSet(author: "efreestone (manual)", id: "202012101335-002") {
        createTable(tableName: "custom_property_blob") {
            column(name: "id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "value", type: "OID") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "efreestone (manual)", id: "202012101335-003") {
        createTable(tableName: "custom_property_boolean") {
            column(name: "id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "value", type: "BOOLEAN") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "efreestone (manual)", id: "202012101335-004") {
        createTable(tableName: "custom_property_container") {
            column(name: "id", type: "BIGINT") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "efreestone (manual)", id: "202012101335-005") {
        createTable(tableName: "custom_property_decimal") {
            column(name: "id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "value", type: "NUMBER(19, 2)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "efreestone (manual)", id: "202012101335-006") {
        createTable(tableName: "custom_property_definition") {
            column(name: "pd_id", type: "VARCHAR(36)") {
                constraints(nullable: "false")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "pd_name", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "pd_type", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "pd_description", type: "VARCHAR(255)")
        }
    }

    changeSet(author: "efreestone (manual)", id: "202012101335-007") {
        createTable(tableName: "custom_property_integer") {
            column(name: "id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "value", type: "INT") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "efreestone (manual)", id: "202012101335-008") {
        createTable(tableName: "custom_property_refdata") {
            column(name: "id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "value_id", type: "VARCHAR(36)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "efreestone (manual)", id: "202012101335-009") {
        createTable(tableName: "custom_property_refdata_definition") {
            column(name: "pd_id", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "category_id", type: "VARCHAR(36)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "efreestone (manual)", id: "202012101335-010") {
        createTable(tableName: "custom_property_text") {
            column(name: "id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "value", type: "CLOB") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "efreestone (manual)", id: "202012101335-011") {
        addPrimaryKey(columnNames: "id", constraintName: "custom_property_blobPK", tableName: "custom_property_blob")
    }

    changeSet(author: "efreestone (manual)", id: "202012101335-012") {
        addPrimaryKey(columnNames: "id", constraintName: "custom_property_booleanPK", tableName: "custom_property_boolean")
    }

    changeSet(author: "efreestone (manual)", id: "202012101335-013") {
        addPrimaryKey(columnNames: "id", constraintName: "custom_property_containerPK", tableName: "custom_property_container")
    }

    changeSet(author: "efreestone (manual)", id: "202012101335-014") {
        addPrimaryKey(columnNames: "id", constraintName: "custom_property_decimalPK", tableName: "custom_property_decimal")
    }

    changeSet(author: "efreestone (manual)", id: "202012101335-015") {
        addPrimaryKey(columnNames: "pd_id", constraintName: "custom_property_definitionPK", tableName: "custom_property_definition")
    }

    changeSet(author: "efreestone (manual)", id: "202012101335-016") {
        addPrimaryKey(columnNames: "id", constraintName: "custom_property_integerPK", tableName: "custom_property_integer")
    }

    changeSet(author: "efreestone (manual)", id: "202012101335-017") {
        addPrimaryKey(columnNames: "id", constraintName: "custom_property_refdataPK", tableName: "custom_property_refdata")
    }

    changeSet(author: "efreestone (manual)", id: "202012101335-018") {
        addPrimaryKey(columnNames: "pd_id", constraintName: "custom_property_refdata_definitionPK", tableName: "custom_property_refdata_definition")
    }

    changeSet(author: "efreestone (manual)", id: "202012101335-019") {
        addPrimaryKey(columnNames: "id", constraintName: "custom_property_textPK", tableName: "custom_property_text")
    }

    changeSet(author: "efreestone (manual)", id: "202012101335-020") {
        createIndex(indexName: "td_type_idx", tableName: "custom_property_definition") {
            column(name: "pd_type")
        }
    }

    changeSet(author: "efreestone (manual)", id: "202012101335-021") {
        addForeignKeyConstraint(baseColumnNames: "definition_id", baseTableName: "custom_property", constraintName: "FK36grvth72fb7wu5i5xaeqjitw", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "pd_id", referencedTableName: "custom_property_definition")
    }

    changeSet(author: "efreestone (manual)", id: "202012101335-022") {
        addForeignKeyConstraint(baseColumnNames: "value_id", baseTableName: "custom_property_refdata", constraintName: "FK5ogn0fedwxxy4fhmq9du4qej2", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value")
    }

    changeSet(author: "efreestone (manual)", id: "202012101335-023") {
        addForeignKeyConstraint(baseColumnNames: "category_id", baseTableName: "custom_property_refdata_definition", constraintName: "FKbrh88caagajlvrpaydg4tr3qx", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdc_id", referencedTableName: "refdata_category")
    }

    changeSet(author: "efreestone (manual)", id: "202012101335-024") {
        addForeignKeyConstraint(baseColumnNames: "parent_id", baseTableName: "custom_property", constraintName: "FKd5u2tgpracxvk1xw8pdreuj5h", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "custom_property_container")
    }

  changeSet(author: "efreestone (manual)", id: "202012101341-1") {
    addColumn(tableName: "custom_property_definition") {
      column (name: "pd_label", type: "VARCHAR(255)") 
      column(name: "pd_weight", type: "INT")
    }
    
    // Add the constraints after adding the data.
    addNotNullConstraint (tableName: "custom_property_definition", columnName: "pd_label" )
    addNotNullConstraint (tableName: "custom_property_definition", columnName: "pd_weight", defaultNullValue: 0)
  }

  changeSet(author: "efreestone (manual)", id: "202012101341-2") {
    addColumn(tableName: "custom_property_definition") {
      column(name: "pd_primary", type: "BOOLEAN")
    }
    addNotNullConstraint (tableName: "custom_property_definition", columnName: "pd_primary", defaultNullValue: 'FALSE')
  }

  changeSet(author: "efreestone (manual)", id: "202012101341-3") {
    addColumn(tableName: "custom_property") {
      column(name: "note", type: "TEXT")
    }
  }

  changeSet(author: "efreestone (manual)", id: "202012101341-4") {
    createIndex(indexName: "td_label_idx", tableName: "custom_property_definition") {
      column(name: "pd_label")
    }
  }

  changeSet(author: "efreestone (manual)", id: "202012101341-5") {
    createIndex(indexName: "td_primary_idx", tableName: "custom_property_definition") {
      column(name: "pd_primary")
    }
  }

  changeSet(author: "efreestone (manual)", id: "202012101341-6") {
    createIndex(indexName: "td_weight_idx", tableName: "custom_property_definition") {
      column(name: "pd_weight")
    }
  }

  changeSet(author: "efreestone (manual)", id: "202012101341-7") {
    addColumn(tableName: "custom_property_definition") {
      column (name: "default_internal", type: "boolean")
    }
    addNotNullConstraint (tableName: "custom_property_definition", columnName: "default_internal" )
  }
  

  changeSet(author: "efreestone (manual)", id: "202012101341-8") {
    addColumn(tableName: "custom_property") {
      column (name: "internal", type: "boolean")
    }
    addNotNullConstraint (tableName: "custom_property", columnName: "internal" )
  }

  changeSet(author: "efreestone (manual)", id: "202012101341-9") {
      addColumn(tableName: "custom_property") {
          column(name: "public_note", type: "text")
      }
  }

  changeSet(author: "efreestone (manual)", id: "202012101341-10") {
    modifyDataType (tableName: 'custom_property_integer', columnName: 'value', newDataType: 'bigint')
  }

  changeSet(author: "efreestone (manual)", id: "202012101341-11") {
    modifyDataType(
      tableName: "custom_property_definition",
      columnName: "pd_description", type: "text",
      newDataType: "text",
      confirm: "Successfully updated the pd_description column."
    )
  }

}