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
import { fireEvent, render } from "@testing-library/react";
import { BoxedExpressionEditor } from "../../../components";
import { DataType, ExpressionProps, LogicType } from "../../../api";
import { act } from "react-dom/test-utils";

describe("BoxedExpressionEditor tests", () => {
  test("should render BoxedExpressionEditor component", () => {
    const selectedExpression: ExpressionProps = { name: "Expression Name", dataType: DataType.Undefined };

    const { container } = render(<BoxedExpressionEditor expressionDefinition={selectedExpression} />);

    expect(container).toMatchSnapshot();
  });
});
