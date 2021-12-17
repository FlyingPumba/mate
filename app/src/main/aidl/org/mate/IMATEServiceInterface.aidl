// IMATEServiceInterface.aidl
package org.mate;

import org.mate.IRepresentationLayerInterface;

interface IMATEServiceInterface {
    void registerRepresentationLayer(IRepresentationLayerInterface representationLayer);
    void reportAvailableActions(in List<String> actions);
}