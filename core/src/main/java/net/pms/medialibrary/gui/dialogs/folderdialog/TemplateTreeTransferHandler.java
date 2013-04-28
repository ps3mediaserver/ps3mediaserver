/*
 * PS3 Media Server, for streaming any medias to your PS3.
 * Copyright (C) 2012  Ph.Waeber
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2
 * of the License only.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package net.pms.medialibrary.gui.dialogs.folderdialog;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import net.pms.medialibrary.commons.dataobjects.DOFileEntryBase;
import net.pms.medialibrary.commons.dataobjects.DOFileEntryFile;
import net.pms.medialibrary.commons.dataobjects.DOFileEntryFolder;
import net.pms.medialibrary.commons.dataobjects.DOFileEntryInfo;
import net.pms.medialibrary.commons.dataobjects.DOFileEntryPlugin;
import net.pms.medialibrary.commons.events.NodeMovedActionListener;

public class TemplateTreeTransferHandler extends TransferHandler {
    private static final long serialVersionUID = -4154706341259947487L;
	private DataFlavor nodesFlavor;
	private DataFlavor[] flavors = new DataFlavor[1];
	private DefaultMutableTreeNode nodeToRemove;
	private List<NodeMovedActionListener> nodeMoveListeners = new ArrayList<NodeMovedActionListener>();
	private DefaultMutableTreeNode _nodeToMove;
	private DefaultMutableTreeNode _nodeToDropOnto;
	private JTree _tree;
	private int _childIndex;
 
    public TemplateTreeTransferHandler() {
        try {
            String mimeType = DataFlavor.javaJVMLocalObjectMimeType + ";class=\"" + javax.swing.tree.DefaultMutableTreeNode[].class.getName() + "\"";
            nodesFlavor = new DataFlavor(mimeType);
            flavors[0] = nodesFlavor;
        } catch(ClassNotFoundException e) {
            System.out.println("ClassNotFound: " + e.getMessage());
        }
    }
    
    public void addNodeMovedActionListener(NodeMovedActionListener listener){
    	if(!this.nodeMoveListeners.contains(listener)){
    		this.nodeMoveListeners.add(listener);
    	}
    }

    @Override
    public boolean canImport(TransferHandler.TransferSupport support) {
        if(!support.isDrop()) {
            return false;
        }
        
        support.setShowDropLocation(true);

        if(!support.isDataFlavorSupported(nodesFlavor)) {
            return false;
        }
        
        _tree = (JTree)support.getComponent();
        JTree.DropLocation dl = (JTree.DropLocation)support.getDropLocation();
        
        //Only allow drops onto DefaultMutableTreeNodes
        if(!(dl.getPath().getLastPathComponent() instanceof DefaultMutableTreeNode)){
        	return false;
        }
        
        
        _nodeToMove = (DefaultMutableTreeNode)_tree.getLastSelectedPathComponent();
        _nodeToDropOnto = (DefaultMutableTreeNode)dl.getPath().getLastPathComponent();

        _childIndex = dl.getChildIndex();
        if(_childIndex == -1 || _nodeToDropOnto.getChildCount() < _childIndex) {
        	if(isFolder((DOFileEntryBase) _nodeToMove.getUserObject())){
        		_childIndex = 0;
        	}else {
            	for(int i = 0; i <_nodeToDropOnto.getChildCount(); i++){
            		if(!isFolder((DOFileEntryBase) ((DefaultMutableTreeNode) _nodeToDropOnto.getChildAt(i)).getUserObject())){
            			_childIndex = i;
            			break;
            		}
            	}  
            	if(_childIndex == -1){
            		_childIndex = _nodeToDropOnto.getChildCount();
            	}      		
        	}
        }
        
        //Only allow drops onto Folders
        if(!(_nodeToDropOnto.getUserObject() instanceof DOFileEntryFolder))
        {
        	return false;
        }
        
        //don't allow drops on itself or own child folder
        DefaultMutableTreeNode parent = _nodeToDropOnto;
        while(parent != null){
        	if(parent.equals(_nodeToMove)){
        		return false;
        	}
        	parent = (DefaultMutableTreeNode)parent.getParent();
        }
        
        //only allow to drop the entry if it's a new location
        if(_childIndex < _nodeToDropOnto.getChildCount() && _nodeToMove.getUserObject().equals(((DefaultMutableTreeNode) _nodeToDropOnto.getChildAt(_childIndex)).getUserObject())){
        	return false;
        }
        
        //only allow file drops:
        //onto index 0 if there aren't any folders
        //under any file (plugin  with !isFolder(), file, file info
        //under the last folder if there are folders
        if(!isFolder((DOFileEntryBase)_nodeToMove.getUserObject())
        		&& ((_childIndex == 0 && (_nodeToDropOnto.getChildCount() > 0 && isFolder((DOFileEntryBase) ((DefaultMutableTreeNode) _nodeToDropOnto.getChildAt(_childIndex)).getUserObject())))
        				|| (_childIndex > 0
        						&& (isFolder((DOFileEntryBase) ((DefaultMutableTreeNode) _nodeToDropOnto.getChildAt(_childIndex - 1)).getUserObject())
        								&& _nodeToDropOnto.getChildCount() > _childIndex
        								&& isFolder((DOFileEntryBase) ((DefaultMutableTreeNode) _nodeToDropOnto.getChildAt(_childIndex)).getUserObject()))))){
        	return false;
        }
        
        //only allow folder drops:
		//onto index 0
		//under any folder
		if (isFolder((DOFileEntryBase)_nodeToMove.getUserObject())
				&& _childIndex > 0 
				&& !isFolder((DOFileEntryBase) ((DefaultMutableTreeNode) _tree.getModel().getChild(_nodeToDropOnto, _childIndex - 1)).getUserObject())) {
			return false; 
		}
        
        return true;
    }
    
    private boolean isFolder(DOFileEntryBase fileEntry){
    	boolean res = true;
    	if(fileEntry instanceof DOFileEntryFile
    			|| (fileEntry instanceof DOFileEntryPlugin && !((DOFileEntryPlugin)fileEntry).getPlugin().isFolder())
    			|| (fileEntry instanceof DOFileEntryInfo)) {
    		res = false;
    	}
    	return res;
    }

    @Override
    protected Transferable createTransferable(JComponent c) {
        JTree tree = (JTree)c;
        TreePath[] paths = tree.getSelectionPaths();
        if(paths != null && paths.length > 0 && paths[0].getLastPathComponent().getClass() == DefaultMutableTreeNode.class) {
            this.nodeToRemove = (DefaultMutableTreeNode) paths[0].getLastPathComponent() ;
            
            return new NodesTransferable(this.nodeToRemove);
        }
        return null;
    }

    @Override
    public int getSourceActions(JComponent c) {
        return MOVE;
    }

    @Override
    public boolean importData(TransferHandler.TransferSupport support) {
        if(!canImport(support)) {
            return false;
        }

        //This avoids having a dropped folder disappear when landing at the bottom of the tree
        if(_nodeToMove.getParent().equals(_nodeToDropOnto) && _childIndex > _nodeToDropOnto.getIndex(_nodeToMove)){
        	_childIndex--;
        }
        
        DefaultTreeModel model = (DefaultTreeModel)_tree.getModel();
        model.removeNodeFromParent(_nodeToMove);
        model.insertNodeInto(_nodeToMove, _nodeToDropOnto, _childIndex);
        _tree.expandPath(new TreePath(_nodeToDropOnto.getPath()));
        
        this._tree = null;
        this._nodeToMove = null;
        this._nodeToDropOnto = null;
        this._childIndex = -1;
        
        return true;
    }

    @Override
    public String toString() {
        return getClass().getName();
    }
 
    public class NodesTransferable implements Transferable {
    	DefaultMutableTreeNode node;
 
        public NodesTransferable(DefaultMutableTreeNode nodeToRemove) {
            this.node = nodeToRemove;
         }

        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
            if(!isDataFlavorSupported(flavor))
                throw new UnsupportedFlavorException(flavor);
            return node;
        }

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return flavors;
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return nodesFlavor.equals(flavor);
        }
    }
}
