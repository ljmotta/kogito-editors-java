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

package org.kie.workbench.common.stunner.client.lienzo.canvas.controls;

import java.util.function.Consumer;
import java.util.function.Function;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Default;
import javax.inject.Inject;

import com.ait.lienzo.client.core.shape.wires.ILineSpliceAcceptor;
import com.ait.lienzo.client.core.shape.wires.WiresConnector;
import com.ait.lienzo.client.core.shape.wires.WiresContainer;
import com.ait.lienzo.client.core.shape.wires.WiresShape;
import com.ait.lienzo.client.core.types.Point2D;
import com.ait.lienzo.client.core.types.Point2DArray;
import org.kie.workbench.common.stunner.client.lienzo.canvas.wires.WiresCanvas;
import org.kie.workbench.common.stunner.client.lienzo.canvas.wires.WiresUtils;
import org.kie.workbench.common.stunner.core.client.canvas.AbstractCanvasHandler;
import org.kie.workbench.common.stunner.core.client.canvas.controls.LineSpliceAcceptorControl;
import org.kie.workbench.common.stunner.core.client.canvas.util.CanvasHighlight;
import org.kie.workbench.common.stunner.core.client.command.CanvasCommandFactory;
import org.kie.workbench.common.stunner.core.client.command.CanvasViolation;
import org.kie.workbench.common.stunner.core.command.Command;
import org.kie.workbench.common.stunner.core.command.CommandResult;
import org.kie.workbench.common.stunner.core.command.impl.CompositeCommand;
import org.kie.workbench.common.stunner.core.command.util.CommandUtils;
import org.kie.workbench.common.stunner.core.graph.Edge;
import org.kie.workbench.common.stunner.core.graph.Node;
import org.kie.workbench.common.stunner.core.graph.content.view.ControlPoint;
import org.kie.workbench.common.stunner.core.graph.content.view.MagnetConnection;
import org.kie.workbench.common.stunner.core.graph.content.view.ViewConnector;

import static org.kie.workbench.common.stunner.client.lienzo.canvas.controls.ContainmentAcceptorControlImpl.areInSameParent;

@Dependent
@Default
public class LineSpliceAcceptorControlImpl
        extends AbstractAcceptorControl
        implements LineSpliceAcceptorControl<AbstractCanvasHandler> {

    private final CanvasCommandFactory<AbstractCanvasHandler> canvasCommandFactory;
    private WiresCanvas canvas;
    private final CanvasHighlight canvasHighlight;

    @Inject
    public LineSpliceAcceptorControlImpl(final CanvasCommandFactory<AbstractCanvasHandler> canvasCommandFactory,
                                         final CanvasHighlight canvasHighlight) {
        this.canvasCommandFactory = canvasCommandFactory;
        this.canvasHighlight = canvasHighlight;
    }

    @Override
    protected void onInit(final WiresCanvas canvas) {
        this.canvas = canvas;
        this.canvas.getWiresManager().setLineSpliceAcceptor(SPLICE_ACCEPTOR);
        this.canvas.getWiresManager().setSpliceEnabled(true);
        canvasHighlight.setCanvasHandler(getCanvasHandler());
    }

    @Override
    protected void onDestroy(final WiresCanvas canvas) {
        canvas.getWiresManager().setLineSpliceAcceptor(ILineSpliceAcceptor.NONE);
        canvasHighlight.destroy();
    }

    @Override
    public boolean allow(final Node spliceNode,
                         final Point2D location,
                         final Node parentNode,
                         final Edge<ViewConnector<?>, Node> edge) {
        return evaluate(spliceNode,
                        location,
                        parentNode,
                        edge,
                        0,
                        null,
                        null,
                        command -> getCommandManager().allow(getCanvasHandler(), command));
    }

    @Override
    public boolean accept(final Node spliceNode,
                          final Point2D location,
                          final Node parentNode,
                          final Edge<ViewConnector<?>, Node> edge,
                          final int controlPoints,
                          final Point2DArray firstHalfPoints,
                          final Point2DArray secondHalfPoints) {
        canvasHighlight.unhighLight();

        return evaluate(spliceNode,
                        location,
                        parentNode,
                        edge,
                        controlPoints,
                        firstHalfPoints,
                        secondHalfPoints,
                        command -> getCommandManager().execute(getCanvasHandler(), command));
    }

    public boolean evaluate(final Node spliceNode,
                            final Point2D location,
                            final Node parentNode,
                            final Edge<ViewConnector<?>, Node> connector,
                            final int controlPoints,
                            final Point2DArray firstHalfPoints,
                            final Point2DArray secondHalfPoints,
                            final Function<Command<AbstractCanvasHandler, CanvasViolation>, CommandResult<CanvasViolation>> executor) {
        final CompositeCommand commands = new CompositeCommand.Builder<AbstractCanvasHandler, CanvasViolation>().build();
        final Node targetNode = connector.getTargetNode();

        // Containment
        if (null != parentNode) {
            if (!areInSameParent(parentNode, new Node[]{spliceNode})) {
                commands.addCommand(canvasCommandFactory.updateChildNode(parentNode, spliceNode));
            }
        }

        commands.addCommand(canvasCommandFactory.updatePosition(spliceNode,
                                                                new org
                                                                        .kie
                                                                        .workbench
                                                                        .common
                                                                        .stunner
                                                                        .core
                                                                        .graph
                                                                        .content
                                                                        .view
                                                                        .Point2D(location.getX(),
                                                                                 location.getY())));

        // Clone and add new control points
        commands.addCommand(canvasCommandFactory
                                    .cloneConnector(connector,
                                                    spliceNode.getUUID(),
                                                    targetNode.getUUID(),
                                                    getCanvasHandler().getDiagram().getMetadata().getShapeSetId(),
                                                    getCloneCallback(spliceNode, commands, secondHalfPoints)));

        if (controlPoints > 0) {
            // Delete current control points between start and end points
            for (int i = 1; i < (controlPoints - 1); i++) {
                commands.addCommand(canvasCommandFactory.deleteControlPoint(connector, 0));
            }
        }

        // Add new control points to original connector
        if (null != firstHalfPoints) {
            // Discard start point
            for (int i = 1; i < firstHalfPoints.size(); i++) {
                final Point2D point = firstHalfPoints.get(i);
                commands.addCommand(canvasCommandFactory
                                            .addControlPoint(connector,
                                                             ControlPoint.build(point.getX(),
                                                                                point.getY()),
                                                             (i - 1)));
            }
        }

        // Connect original connector to splice node
        commands.addCommand(canvasCommandFactory.setTargetNode(spliceNode,
                                                               connector,
                                                               MagnetConnection.Builder.atCenter(spliceNode)));

        final boolean accepts = executeCommands(executor, commands);

        highlight(connector, accepts);

        return accepts;
    }

    // Execute allow/accept
    protected boolean executeCommands(final Function<Command<AbstractCanvasHandler, CanvasViolation>, CommandResult<CanvasViolation>> executor,
                                      final CompositeCommand commands) {
        return !CommandUtils.isError(executor.apply(commands));
    }

    private Consumer<Edge> getCloneCallback(final Node spliceNode,
                                            final CompositeCommand commands,
                                            final Point2DArray controlPoints) {
        return edge -> {
            // Connect the new connector to splice node
            commands.addCommand(canvasCommandFactory.setSourceNode(spliceNode,
                                                                   edge,
                                                                   MagnetConnection.Builder.atCenter(spliceNode)));

            // Add new control points to new connector
            if (null != controlPoints) {
                // Discard end point
                for (int i = 0; i < (controlPoints.size() - 1); i++) {
                    final Point2D point = controlPoints.get(i);
                    commands.addCommand(canvasCommandFactory
                                                .addControlPoint(edge,
                                                                 ControlPoint.build(point.getX(), point.getY()),
                                                                 i));
                }
            }
        };
    }

    private final ILineSpliceAcceptor SPLICE_ACCEPTOR = new ILineSpliceAcceptor() {
        @Override
        public boolean allowSplice(final WiresShape spliceShape,
                                   final Point2D candidateLocation,
                                   final WiresConnector connector,
                                   final WiresContainer parent) {
            final Edge edge = WiresUtils.getEdge(getCanvasHandler(), connector);
            final Node spliceNode = WiresUtils.getNode(getCanvasHandler(), spliceShape);

            if ((null != parent && !isWiresParentAccept(parent)) ||
                    null == edge.getSourceNode() ||
                    null == edge.getTargetNode()) {
                return false;
            }

            return allow(spliceNode,
                         candidateLocation,
                         WiresUtils.getNode(getCanvasHandler(), parent),
                         edge);
        }

        @Override
        public boolean acceptSplice(final WiresShape spliceShape,
                                    final Point2D candidateLocation,
                                    final WiresConnector connector,
                                    final Point2DArray firstHalfPoints,
                                    final Point2DArray secondHalfPoints,
                                    final WiresContainer parent) {
            final Edge edge = WiresUtils.getEdge(getCanvasHandler(), connector);
            final Node spliceNode = WiresUtils.getNode(getCanvasHandler(), spliceShape);

            if ((null != parent && !isWiresParentAccept(parent)) ||
                    null == edge.getSourceNode() ||
                    null == edge.getTargetNode()) {
                return false;
            }

            return accept(spliceNode,
                          candidateLocation,
                          WiresUtils.getNode(getCanvasHandler(), parent),
                          edge,
                          connector.getControlPoints().size(),
                          firstHalfPoints,
                          secondHalfPoints);
        }

        public void ensureUnHighLight() {
            canvasHighlight.unhighLight();
        }
    };

    private void highlight(final Edge<ViewConnector<?>, Node> connector,
                           final boolean valid) {
        canvasHighlight.unhighLight();

        if (valid) {
            canvasHighlight.highLight(connector);
        } else {
            canvasHighlight.invalid(connector);
        }
    }
}
