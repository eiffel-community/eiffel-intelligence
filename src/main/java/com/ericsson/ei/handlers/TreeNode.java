/*
    Copyright 2017 Ericsson AB.
    For a full list of individual contributors, please see the commit history.
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
    http://www.apache.org/licenses/LICENSE-2.0
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/

package com.ericsson.ei.handlers;

import java.util.ArrayList;
import java.util.List;

/**
 * The Class TreeNode.
 *
 * @param <T> the generic type
 */
public class TreeNode<T>{

    /** The data of the generic type T associated with this node.*/
    private T data = null;

    /** A list of all the tree nodes this node has as children. */
    private List<TreeNode> children = new ArrayList<>();

    /** The tree node this node has as parent. */
    private TreeNode parent = null;

    /**
     * Instantiates a new tree node.
     *
     * @param data
     *          data of the generic type T associated with this node
     */
    public TreeNode(T data) {
        this.data = data;
    }

    /**
     * Adds a child to this node.
     *
     * @param child
     *          the tree node child
     */
    public void addChild(TreeNode child) {
        child.setParent(this);
        this.children.add(child);
    }

    /**
     * Creates and adds a child tree node to this tree node.
     *
     * @param data
     *          the data of the generic type T associated with the child node.
     */
    public void addChild(T data) {
        TreeNode<T> newChild = new TreeNode<>(data);
        newChild.setParent(this);
        children.add(newChild);
    }

    /**
     * Adds a list of children to this node.
     *
     * @param children the children
     */
    public void addChildren(List<TreeNode> children) {
        for(TreeNode t : children) {
            t.setParent(this);
        }
        this.children.addAll(children);
    }

    /**
     * Gets a list of all the children of this node.
     *
     * @return a tree node list of all the children of this node
     */
    public List<TreeNode> getChildren() {
        return children;
    }

    /**
     * Gets the data.
     *
     * @return data of the generic type T associated with this node
     */
    public T getData() {
        return data;
    }

    /**
     * Sets the data of the generic type T associated with this node.
     *
     * @param data
     *           the new data of the generic type T
     */
    public void setData(T data) {
        this.data = data;
    }

    /**
     * Sets the parent.
     *
     * @param parent
     *           the new parent
     */
    private void setParent(TreeNode parent) {
        this.parent = parent;
    }

    /**
     * Gets the parent.
     *
     * @return
     *      the parent
     */
    public TreeNode getParent() {
        return parent;
    }
}
