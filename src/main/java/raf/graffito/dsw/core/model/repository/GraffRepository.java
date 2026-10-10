// raf/graffito/dsw/core/model/repository/GraffRepository.java
package raf.graffito.dsw.core.model.repository;

import raf.graffito.dsw.core.model.factory.graffNode.ChildType;
import raf.graffito.dsw.core.model.repository.composite.GraffNode;
import raf.graffito.dsw.core.model.repository.composite.GraffNodeComposite;
import raf.graffito.dsw.core.model.repository.implementation.Workspace;

public interface GraffRepository {
    Workspace getWorkspace();
    GraffNode addChild(GraffNodeComposite parent);
    GraffNode addChild(GraffNodeComposite parent, ChildType type);
    void deleteChild(GraffNode child);
    void attachChild(GraffNodeComposite parent, GraffNode child, int index);
}
