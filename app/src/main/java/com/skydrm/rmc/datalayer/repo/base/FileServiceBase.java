package com.skydrm.rmc.datalayer.repo.base;

import com.skydrm.rmc.exceptions.InvalidRMClientException;
import com.skydrm.rmc.exceptions.SessionInvalidException;
import com.skydrm.sdk.exception.RmsRestAPIException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class FileServiceBase<T extends IFileType, ITEM extends IFileType> implements IFileService<T> {

    protected abstract List<T> syncCurrentPath(String pathId)
            throws SessionInvalidException, InvalidRMClientException, RmsRestAPIException;

    protected abstract List<T> syncTree(String pathId)
            throws SessionInvalidException, InvalidRMClientException, RmsRestAPIException;

    protected abstract List<ITEM> listInternal(String pathId);

    protected abstract T newByDBItem(ITEM item);

    protected abstract T newByDBItem(ITEM item, List<T> children);

    @Override
    public List<T> listFile(String pathId, boolean recursively) {
        return recursively ? listTree(pathId) : listCurrentPath(pathId);
    }

    @Override
    public List<T> syncFile(String pathId, boolean recursively)
            throws SessionInvalidException, InvalidRMClientException, RmsRestAPIException {
        return recursively ? syncTree(pathId) : syncCurrentPath(pathId);
    }

    protected List<T> listCurrentPath(String pathId) {
        return adapt2NxlItem(listByLayer(pathId));
    }

    protected List<T> listTree(String pathId) {
        return adaptList2Tree(listInternal(pathId), pathId);
    }

    protected List<ITEM> listByLayer(String pathId) {
        return listByLayer(listInternal(pathId), pathId);
    }

    protected List<ITEM> listByLayer(List<ITEM> treeList, String pathId) {
        List<ITEM> ret = new ArrayList<>();
        if (treeList == null || treeList.isEmpty()) {
            return ret;
        }
        String parent;
        if (pathId == null || pathId.isEmpty()) {
            parent = "/";
        } else {
            parent = pathId;
        }
        for (ITEM item : treeList) {
            if (Utils.isDirectChild(parent, item.getPathId())) {
                ret.add(item);
            }
        }
        return ret;
    }

    protected List<ITEM> buildLocalTreeList(String pathId) {
        List<ITEM> ret = new ArrayList<>();

        List<ITEM> results = listByLayer(pathId);
        if (results == null || results.isEmpty()) {
            return ret;
        }
        for (ITEM item : results) {
            ret.add(item);
            if (item.isFolder()) {
                ret.addAll(buildLocalTreeList(item.getPathId()));
            }
        }
        return ret;
    }

    protected List<T> adapt2NxlItem(List<ITEM> lfs) {
        List<T> ret = new ArrayList<>();
        if (lfs == null || lfs.size() == 0) {
            return ret;
        }
        for (ITEM i : lfs) {
            if (i.isFolder()) {
                ret.add(newByDBItem(i, null));
            } else {
                ret.add(newByDBItem(i));
            }
        }
        return ret;
    }

    protected List<T> adaptList2Tree(List<ITEM> tree, String parent) {
        return findChildren(tree, parent);
    }

    private List<T> findChildren(List<ITEM> tree, String parent) {
        List<T> root = new ArrayList<>();
        if (tree == null || tree.isEmpty()) {
            return root;
        }
        Iterator<ITEM> it = tree.iterator();
        while (it.hasNext()) {
            ITEM next = it.next();
            String childPathId = next.getPathId();
            if (Utils.isDirectChild(parent, childPathId)) {
                if (next.isFolder()) {
                    root.add(newByDBItem(next,
                            findChildren(new ArrayList<>(tree), next.getPathId())));
                } else {
                    root.add(newByDBItem(next));
                }
                it.remove();
            }
        }
        return root;
    }

}
