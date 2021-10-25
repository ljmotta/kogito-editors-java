/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import "./InvocationExpression.css";
import * as React from "react";
import { useCallback, useContext, useEffect, useMemo, useRef, useState } from "react";
import {
  ContextEntries,
  ContextEntryRecord,
  DataType,
  DEFAULT_ENTRY_EXPRESSION_MIN_WIDTH,
  DEFAULT_ENTRY_INFO_MIN_WIDTH,
  EntryInfo,
  executeIfExpressionDefinitionChanged,
  generateNextAvailableEntryName,
  getEntryKey,
  getHandlerConfiguration,
  InvocationProps,
  LogicType,
  resetEntry,
  TableHeaderVisibility,
} from "../../api";
import { Table } from "../Table";
import { useBoxedExpressionEditorI18n } from "../../i18n";
import { ColumnInstance, DataRecord } from "react-table";
import { ContextEntryExpressionCell, getContextEntryInfoCell } from "../ContextExpression";
import * as _ from "lodash";
import { BoxedExpressionGlobalContext } from "../../context";
import { hashfy } from "../Resizer";

const DEFAULT_PARAMETER_NAME = "p-1";
const DEFAULT_PARAMETER_DATA_TYPE = DataType.Undefined;

export const InvocationExpression: React.FunctionComponent<InvocationProps> = (invocationProps: InvocationProps) => {
  const { i18n } = useBoxedExpressionEditorI18n();

  const rows = useMemo(() => {
    return (
      invocationProps.bindingEntries || [
        {
          entryInfo: {
            name: DEFAULT_PARAMETER_NAME,
            dataType: DEFAULT_PARAMETER_DATA_TYPE,
          },
          entryExpression: {
            name: DEFAULT_PARAMETER_NAME,
            dataType: DEFAULT_PARAMETER_DATA_TYPE,
          },
          nameAndDataTypeSynchronized: true,
        } as DataRecord,
      ]
    );
  }, [invocationProps.bindingEntries]);

  const [infoWidth, setInfoWidth] = useState(invocationProps.entryInfoWidth ?? DEFAULT_ENTRY_INFO_MIN_WIDTH);
  const [expressionWidth, setExpressionWidth] = useState(
    invocationProps.entryExpressionWidth ?? DEFAULT_ENTRY_EXPRESSION_MIN_WIDTH
  );
  const [functionName, setFunctionName] = useState(invocationProps.invokedFunction);
  const { setSupervisorHash } = useContext(BoxedExpressionGlobalContext);

  const onBlurCallback = useCallback((event) => {
    setFunctionName(event.target.value);
  }, []);

  const headerCellElement = useMemo(
    () => (
      <div className="function-definition-container">
        <input
          className="function-definition pf-u-text-truncate"
          type="text"
          placeholder={i18n.enterFunction}
          defaultValue={functionName}
          onBlur={onBlurCallback}
        />
      </div>
    ),
    [functionName, onBlurCallback, i18n.enterFunction]
  );

  const columns = useMemo(
    () => [
      {
        label: invocationProps.name ?? DEFAULT_PARAMETER_NAME,
        accessor: invocationProps.name ?? DEFAULT_PARAMETER_NAME,
        dataType: invocationProps.dataType ?? DEFAULT_PARAMETER_DATA_TYPE,
        disableHandlerOnHeader: true,
        columns: [
          {
            headerCellElement,
            accessor: "functionDefinition",
            disableHandlerOnHeader: true,
            columns: [
              {
                accessor: "entryInfo",
                disableHandlerOnHeader: true,
                width: invocationProps.entryInfoWidth ?? infoWidth,
                setWidth: setInfoWidth,
                minWidth: DEFAULT_ENTRY_INFO_MIN_WIDTH,
              },
              {
                accessor: "entryExpression",
                disableHandlerOnHeader: true,
                width: invocationProps.entryExpressionWidth ?? expressionWidth,
                setWidth: setExpressionWidth,
                minWidth: DEFAULT_ENTRY_EXPRESSION_MIN_WIDTH,
              },
            ],
          },
        ],
      },
    ],
    [invocationProps, infoWidth, expressionWidth, headerCellElement]
  );

  const spreadInvocationExpressionDefinition = useCallback(
    (invocationExpressionUpdated?: Partial<InvocationProps>) => {
      const updatedDefinition: InvocationProps = {
        uid: invocationProps.uid,
        logicType: LogicType.Invocation,
        name: invocationProps.name ?? DEFAULT_PARAMETER_NAME,
        dataType: invocationProps.dataType ?? DEFAULT_PARAMETER_DATA_TYPE,
        bindingEntries: rows as ContextEntries,
        invokedFunction: functionName,
        entryInfoWidth: infoWidth,
        entryExpressionWidth: expressionWidth,
        ...invocationExpressionUpdated,
      };

      const expression = _.omit(updatedDefinition, ["name", "dataType"]);

      if (invocationProps.isHeadless) {
        invocationProps.onUpdatingRecursiveExpression?.(expression);
      } else {
        executeIfExpressionDefinitionChanged(
          invocationProps,
          updatedDefinition,
          () => {
            setSupervisorHash(hashfy(expression));
            window.beeApi?.broadcastInvocationExpressionDefinition?.(updatedDefinition);
          },
          ["name", "dataType", "bindingEntries", "invokedFunction", "entryInfoWidth", "entryExpressionWidth"]
        );
      }
    },
    [invocationProps, expressionWidth, functionName, infoWidth, rows, setSupervisorHash]
  );

  useEffect(() => {
    spreadInvocationExpressionDefinition();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [rows, functionName]);

  const onColumnsUpdate = useCallback(
    ([expressionColumn]: [ColumnInstance]) => {
      invocationProps.onUpdatingNameAndDataType?.(expressionColumn.label as string, expressionColumn.dataType);

      spreadInvocationExpressionDefinition({
        name: expressionColumn.label as string,
        dataType: expressionColumn.dataType,
      });
    },
    [invocationProps, spreadInvocationExpressionDefinition]
  );

  const onRowAdding = useCallback(() => {
    const generatedName = generateNextAvailableEntryName(
      _.map(rows, (row: ContextEntryRecord) => row.entryInfo) as EntryInfo[],
      "p"
    );
    return {
      entryInfo: {
        name: generatedName,
        dataType: DEFAULT_PARAMETER_DATA_TYPE,
      },
      entryExpression: {
        name: generatedName,
        dataType: DEFAULT_PARAMETER_DATA_TYPE,
      },
      nameAndDataTypeSynchronized: true,
    };
  }, [rows]);

  const getHeaderVisibility = useMemo(
    () => (invocationProps.isHeadless ? TableHeaderVisibility.SecondToLastLevel : TableHeaderVisibility.Full),
    [invocationProps.isHeadless]
  );

  const setRowsCallback = useCallback(
    (entries) => {
      spreadInvocationExpressionDefinition({ bindingEntries: [...entries] });
    },
    [spreadInvocationExpressionDefinition]
  );

  const getRowKeyCallback = useCallback((row) => getEntryKey(row), []);
  const resetEntryCallback = useCallback((row) => resetEntry(row), []);

  const defaultCell = useMemo(
    () => ({
      entryInfo: getContextEntryInfoCell(i18n.editParameter),
      entryExpression: ContextEntryExpressionCell,
    }),
    [i18n.editParameter]
  );

  const handlerConfiguration = useMemo(() => {
    return getHandlerConfiguration(i18n, i18n.parameters);
  }, [i18n]);

  return (
    <div className={`invocation-expression ${invocationProps.uid}`}>
      <Table
        tableId={invocationProps.uid}
        headerLevels={2}
        headerVisibility={getHeaderVisibility}
        skipLastHeaderGroup={true}
        defaultCell={defaultCell}
        columns={columns}
        rows={rows as DataRecord[]}
        onColumnsUpdate={onColumnsUpdate}
        onRowAdding={onRowAdding}
        onRowsUpdate={setRowsCallback}
        handlerConfiguration={handlerConfiguration}
        getRowKey={getRowKeyCallback}
        resetRowCustomFunction={resetEntryCallback}
      />
    </div>
  );
};
