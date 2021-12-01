/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import * as React from "react";
import { useState } from "react";
import * as ReactDOM from "react-dom";
import "./index.css";
import { I18nDictionariesProvider } from "@kogito-tooling/i18n/dist/react-components";
// noinspection ES6PreferShortImport
import {
  BoxedExpressionProvider,
  ContextProps,
  DataType,
  DecisionTableProps,
  ExpressionProps,
  FunctionProps,
  InvocationProps,
  ListProps,
  LiteralExpressionProps,
  LogicType,
  RelationProps,
} from "./lib";
import "./lib/components/BoxedExpressionEditor/base-no-reset-wrapped.css";
import { LiteralExpression } from "./lib/components/LiteralExpression";
import {
  boxedExpressionEditorDictionaries,
  BoxedExpressionEditorI18nContext,
  boxedExpressionEditorI18nDefaults,
} from "./lib/i18n";

export const App: React.FunctionComponent = () => {
  //This definition comes directly from the decision node
  const selectedExpression: ExpressionProps = {
    name: "Expression Name",
    dataType: DataType.Undefined,
  };

  const pmmlParams = [
    {
      document: "mining pmml",
      modelsFromDocument: [
        {
          model: "MiningModelSum",
          parametersFromModel: [
            { name: "input1", dataType: DataType.Any },
            { name: "input2", dataType: DataType.Any },
            { name: "input3", dataType: DataType.Any },
          ],
        },
      ],
    },
    {
      document: "regression pmml",
      modelsFromDocument: [
        {
          model: "RegressionLinear",
          parametersFromModel: [
            { name: "i1", dataType: DataType.Number },
            { name: "i2", dataType: DataType.Number },
          ],
        },
      ],
    },
  ];

  const [expressionDefinition, setExpressionDefinition] = useState(selectedExpression);
  const [otherExpression, setOtherExpression] = useState(selectedExpression);

  //Defining global function that will be available in the Window namespace and used by the BoxedExpressionEditor component
  window.beeApi = {
    resetExpressionDefinition: (definition: ExpressionProps) => setExpressionDefinition(definition),
    broadcastLiteralExpressionDefinition: (definition: LiteralExpressionProps) => {
      setExpressionDefinition(definition);
    },
    broadcastRelationExpressionDefinition: (definition: RelationProps) => setExpressionDefinition(definition),
    broadcastContextExpressionDefinition: (definition: ContextProps) => setExpressionDefinition(definition),
    broadcastListExpressionDefinition: (definition: ListProps) => setExpressionDefinition(definition),
    broadcastInvocationExpressionDefinition: (definition: InvocationProps) => setExpressionDefinition(definition),
    broadcastFunctionExpressionDefinition: (definition: FunctionProps) => setExpressionDefinition(definition),
    broadcastDecisionTableExpressionDefinition: (definition: DecisionTableProps) => setExpressionDefinition(definition),
  };

  return (
    <div className="showcase">
      <div className="boxed-expression">
        <I18nDictionariesProvider
          defaults={boxedExpressionEditorI18nDefaults}
          dictionaries={boxedExpressionEditorDictionaries}
          initialLocale={navigator.language}
          ctx={BoxedExpressionEditorI18nContext}
        >
          <BoxedExpressionProvider
            expressionDefinition={expressionDefinition}
            isRunnerTable={false}
            pmmlParams={pmmlParams}
          >
            <div className="expression-container">
              <div className="expression-container-box" data-ouia-component-id="expression-container">
                <LiteralExpression
                  {...(expressionDefinition as LiteralExpressionProps)}
                  logicType={LogicType.LiteralExpression}
                />
              </div>
            </div>
          </BoxedExpressionProvider>
          <BoxedExpressionProvider expressionDefinition={otherExpression} isRunnerTable={false} pmmlParams={pmmlParams}>
            <div className="expression-container">
              <div className="expression-container-box" data-ouia-component-id="expression-container">
                <LiteralExpression
                  {...(otherExpression as LiteralExpressionProps)}
                  logicType={LogicType.LiteralExpression}
                />
              </div>
            </div>
          </BoxedExpressionProvider>
        </I18nDictionariesProvider>
      </div>
    </div>
  );
};

ReactDOM.render(<App />, document.getElementById("root"));
