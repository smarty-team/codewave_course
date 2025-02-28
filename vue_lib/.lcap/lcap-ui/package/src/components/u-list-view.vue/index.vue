<template>
<div :class="$style.root" :readonly="readonly" :readonly-mode="readonlyMode" :disabled="disabled" :border="String(border)"
    :tabindex="readonly || disabled ? '' : 0"
    @keydown.prevent.stop.up="shift(-1)"
    @keydown.prevent.stop.down="shift(+1)">
    <div v-show="showHead" :class="$style.head">
        <slot name="head">
            <u-checkbox v-if="multiple" :value="allChecked" @check="checkAll($event.value)" :readonly="readonly" :disabled="disabled"></u-checkbox>
            <span :class="$style.title" vusion-slot-name="title">{{ title }}</span>
            <div :class="$style.extra">
                <span v-if="multiple">{{ selectedVMs.length }}{{ currentDataSource && currentDataSource.originTotal !== Infinity ? ' / ' + currentDataSource.originTotal : '' }}</span>
            </div>
        </slot>
    </div>
    <u-input v-if="filterable" :class="$style.filter" :disabled="disabled" :placeholder="placeholder" :size="filterSize" prefix="search" :clearable="clearable"
        :value="filterText" @input="onInput">
    </u-input>
    <div ref="body" :class="$style.body" @scroll.stop="onScroll">
        <slot></slot>
        <div ref="virtual" v-if="(!currentLoading && !currentError && !currentEmpty || pageable === 'auto-more' || pageable === 'load-more') && currentData && currentData.length"
            :style="{ paddingTop: virtualTop + 'px', paddingBottom: virtualBottom + 'px' }">

            <component :is="ChildComponent"
                v-for="(item, index) in virtualList"
                v-if="item !== undefined || item !== null"
                :key="$at(item, valueField) || item"
                :value="$at(item, valueField) || item"
                :disabled="item.disabled || disabled"
                :ellipsis-title="ellipsisTitle"
                :item="item"
                :index="index">
                <slot name="item" :item="item" :index="index" :text="$at(item, field || textField) || item" :value="$at(item, valueField) || item" :disabled="item.disabled || disabled" vusion-slot-name="item" :ellipsis-title="ellipsisTitle">{{ isPrimitive(item) ? item : $at(item, field || textField) }}<s-empty v-if="(!$slots.item) && $env.VUE_APP_DESIGNER && !!$attrs['vusion-node-path']"></s-empty></slot>
            </component>
        </div>
        <div :class="$style.status" status="loading" v-if="currentLoading" vusion-slot-name="loading">
            <slot name="loading"><u-spinner></u-spinner> {{ loadingText }}</slot>
            <s-empty v-if="$env.VUE_APP_DESIGNER
                && !$slots.loading
                && $scopedSlots
                && !($scopedSlots.loading && $scopedSlots.loading())
                && !!$attrs['vusion-node-path']">
            </s-empty>
        </div>
        <div :class="$style.status" status="error" v-else-if="currentData === null || currentError" vusion-slot-name="error">
            <slot name="error">{{ errorText }}</slot>
            <s-empty v-if="$env.VUE_APP_DESIGNER
                && !$slots.error
                && $scopedSlots
                && !($scopedSlots.error && $scopedSlots.error())
                && !!$attrs['vusion-node-path']">
            </s-empty>
        </div>
        <div :class="$style.status" v-else-if="pageable === 'load-more' && currentDataSource.hasMore()">
            <u-link @click="load(true)">{{ $tt('loadMore') }}</u-link>
        </div>
        <div :class="$style.status" v-else-if="((pageable === 'auto-more' && hasScroll) || pageable === 'load-more') && !currentDataSource.hasMore() && (currentData && currentData.length)">
            {{ $tt('noMore') }}
        </div>
        <div :class="$style.status" v-else-if="currentData && !currentData.length || currentEmpty" vusion-slot-name="empty">
            <slot name="empty">{{ emptyText }}</slot>
            <s-empty v-if="$env.VUE_APP_DESIGNER
                && !$slots.empty
                && $scopedSlots
                && !($scopedSlots.empty && $scopedSlots.empty())
                && !!$attrs['vusion-node-path']">
            </s-empty>
        </div>
    </div>
    <div v-show="showFoot && (pageable === true || pageable === 'pagination')" :class="$style.foot">
        <slot name="foot"></slot>
        <u-pagination :class="$style.pagination" v-if="pageable === true || pageable === 'pagination'" ref="pagination"
            :total-items="currentDataSource.total" :page="currentDataSource.paging ? currentDataSource.paging.number : pageNumber"
            :page-size="currentDataSource.paging ? currentDataSource.paging.size : pageSize" :page-size-options="pageSizeOptions" :show-total="showTotal" :show-sizer="showSizer" :show-jumper="showJumper"
            :side="1" :around="3"
            @change="page($event.page)" @change-page-size="onChangePageSize">
        </u-pagination>
    </div>
</div>
</template>

<script>
import { sync } from '@lcap/vue2-utils';
import { MComplex } from '../m-complex.vue';
import { MGroupParent } from '../m-group.vue';
import MField from '../m-field.vue';
import FVirtualList from '../f-virtual-list.vue';
import DataSource from '../../utils/DataSource';
import debounce from 'lodash/debounce';
import i18n from './i18n';
import { findScrollParent } from '../../utils/dom';
import SEmpty from '../s-empty.vue';
import i18nMixin from '../../mixins/i18n';

export default {
    name: 'u-list-view',
    groupName: 'u-list-view-group',
    childName: 'u-list-view-item',
    components: {
        SEmpty,
    },
    mixins: [
      MComplex,
      MGroupParent,
      MField,
      FVirtualList,
      i18nMixin('u-list-view'),
      sync({
        data: 'currentData',
        total() {
          return this.currentDataSource && this.currentDataSource.total ? this.currentDataSource.total : 0;
        },
        size() {
          return this.currentDataSource && this.currentDataSource.paging ? this.currentDataSource.paging.size : this.pageSize;
        },
        page() {
          return this.currentDataSource && this.currentDataSource.paging ? this.currentDataSource.paging.number : this.pageNumber;
        },
        sort() {
          return this.currentDataSource && this.currentDataSource.sorting ? this.currentDataSource.sorting.field : '';
        },
        order() {
          return this.currentDataSource && this.currentDataSource.sorting ? this.currentDataSource.sorting.order : '';
        },
        filterText: 'filterText',
        readonly: 'readonly',
        disabled: 'disabled',
        value() {
          if (this.currentMultiple) {
            return this.selectedVMs.map((itemVM) => itemVM.value);
          }

          return this.selectedVM ? this.selectedVM.value : undefined;
        },
      })
    ],
    // i18n,
    props: {
        // @inherit: value: null,
        // @inherit: value: Array,
        field: String,
        textField: { type: String, default: 'text' },
        valueField: { type: String, default: 'value' },
        data: Array,
        dataSource: [DataSource, Function, Object, Array],
        // @inherit: cancelable: { type: Boolean, default: false },
        // @inherit: multiple: { type: Boolean, default: false },
        // @inherit: collapsible: { type: Boolean, default: false },
        // @inherit: accordion: { type: Boolean, default: false },
        // @inherit: expandTrigger: { type: String, default: 'click' },
        // @inherit: readonly: { type: Boolean, default: false },
        readonlyMode: String,
        // @inherit: disabled: { type: Boolean, default: false },
        showHead: { type: Boolean, default: false },
        title: { type: String, default: '列表' },
        showFoot: { type: Boolean, default: true },
        border: { type: Boolean, default: true },
        loading: { type: Boolean, default: false },
        loadingText: {
            type: String,
            default() {
                return this.$tt('loading');
            },
        },
        error: { type: Boolean, default: false },
        errorText: {
            type: String,
            default() {
                return this.$tt('error');
            },
        },
        emptyText: {
            type: String,
            default() {
                return this.$tt('empty');
            },
        },
        initialLoad: { type: Boolean, default: true },
        filterable: { type: Boolean, default: false },
        filterSize: { type: String, default: 'small' },
        placeholder: { type: String, default: '请输入' },
        clearable: { type: Boolean, default: true },
        matchMethod: { type: [String, Function], default: 'includes' },
        caseSensitive: { type: Boolean, default: false },
        remoteFiltering: { type: Boolean, default: false },
        pageable: { type: [Boolean, String], default: false },
        pageSize: { type: Number, default: 50 },
        pageNumber: { type: Number, default: 1 },
        pageSizeOptions: {
            type: Array,
            default() {
                return [10, 20, 50];
            },
        },
        showTotal: { type: Boolean, default: false },
        showSizer: { type: Boolean, default: false },
        showJumper: { type: Boolean, default: false },
        remotePaging: { type: Boolean, default: false },
        virtual: { type: Boolean, default: false },
        // @inherit: virtualCount: { type: Number, default: 60 },
        // @inherit: throttle: { type: Number, default: 60 },
        listKey: { type: String, default: 'currentData' },
        checkbox: { type: Boolean, default: false },
        ellipsisTitle: { type: [Boolean, String], default: false },
        selectedValuesData: Array, // 如果是分页数据，选中的值在可能在下拉框里没有，导致选中值展示不出来。这里传入该字段，用于展示选中的值
        designerMode: { type: String, default: 'success' }, // 配合IDE编辑器展示不同表格状态
    },
    data() {
        return {
            ChildComponent: this.$options.childName,
            // @inherit: groupVMs: [],
            // @inherit: itemVMs: [],
            // @inherit: selectedVM: undefined,
            // @inherit: selectedVMs: undefined,
            // @inherit: currentMultiple: this.multiple,
            currentDataSource: undefined,
            currentLoading: this.loading,
            currentError: this.error,
            currentEmpty: false, // 配合IDE
            // virtualIndex: 0,
            // virtualTop: 0,
            // virtualBottom: 0,
            filterText: '', // 过滤文本，只有 input 时会改变它
            hasScroll: false, // 作为下拉加载是否展示"没有更多"的依据。第一页不满，没有滚动条的情况下，不展示
            currentPageSize: undefined, // 区分是pageSize还是pageSizeOptions改变
        };
    },
    computed: {
        currentData() {
            return this.currentDataSource && this.currentDataSource.viewData;
        },
        paging() {
            if (this.pageable) {
                const paging = {};
                let currentPageSize = this.currentPageSize !== undefined ? this.currentPageSize : this.pageSize;
                currentPageSize = currentPageSize === '' ? 50 : currentPageSize;
                paging.size = currentPageSize;
                paging.number = this.pageNumber;
                return paging;
            } else
                return undefined;
        },
        filtering() {
            return {
                [this.field || this.textField]: {
                    operator: this.matchMethod,
                    value: this.filterText,
                    caseInsensitive: !this.caseSensitive,
                },
            };
        },
        allChecked() {
            if (!this.currentDataSource)
                return; // 仅显示当前页的选中项
            let checkedLength = 0;
            this.itemVMs.forEach((itemVM) => {
                if (itemVM.currentSelected)
                    checkedLength++;
            });
            if (checkedLength === 0)
                return false;
            else if (checkedLength === this.currentData.length)
                return true;
            else
                return null;
        },
    },
    watch: {
        data(data) {
            this.handleData();
        },
        dataSource(dataSource, oldDataSource) {
            if (typeof dataSource === 'function' && String(dataSource) === String(oldDataSource))
                return;
            this.handleData();
        },
        loading(loading) {
            this.currentLoading = loading;
        },
        error(error) {
            this.currentError = error;
        },
        pageable() {
            this.$forceUpdate();
        },
        itemVMs: {
            override: true,
            handler(itemVMs, oldVMs) {
                if (this.data || this.dataSource)
                    return;
                MComplex.watch.itemVMs.handler.call(this, itemVMs, oldVMs);
            },
        },
        currentData() { // 下拉加载更多的情况下，itemVMs的watch没有调用，这里增加调用，用于勾选选中的值
            this.$nextTick(() => {
                MComplex.watch.itemVMs.handler.call(this, this.itemVMs);
            });
        },
        designerMode: {
            handler(designerMode) {
                if (!this.$env.VUE_APP_DESIGNER)
                    return;
                switch (designerMode) {
                    case 'loading':
                        this.currentLoading = true;
                        this.currentError = false;
                        this.currentEmpty = false;
                        break;
                    case 'error':
                        this.currentLoading = false;
                        this.currentError = true;
                        this.currentEmpty = false;
                        break;
                    case 'empty':
                        this.currentLoading = false;
                        this.currentError = false;
                        this.currentEmpty = true;
                        break;
                    default:
                        this.currentLoading = this.loading;
                        this.currentError = this.error;
                        this.currentEmpty = false;
                }
            },
            immediate: true,
        },
        pageSize() {
            this.currentPageSize = undefined;
        },
        paging: {
            handler(value) {
                if (this.currentDataSource) {
                    if (this.currentDataSource.paging.number !== value.number || this.currentDataSource.paging.size !== value.size)
                        this.page(value.number, value.size);
                }
            },
            deep: true,
        },
    },
    created() {
        // 自动补充 pageSizeOptions
        if (this.pageSizeOptions && !this.pageSizeOptions.includes(this.pageSize)) {
            for (let i = 0; i < this.pageSizeOptions.length; i++) {
                if (this.pageSizeOptions[i] > this.pageSize) {
                    this.pageSizeOptions.splice(i, 0, this.pageSize);
                    break;
                }
            }
        }

        // this.$watch('pageNumber', (number) => {
        //     if (this.currentDataSource && this.currentDataSource.paging.number !== number)
        //         this.page(number);
        // });

        this.debouncedLoad = debounce(this.load, 300);
        this.currentDataSource = this.normalizeDataSource(this.dataSource || this.data);

        if (this.currentDataSource && this.initialLoad) {
            if (this.pageNumber && (typeof this.pagination === 'undefined' ? this.pageable : this.pagination)) {
                this.page(this.pageNumber);
            } else {
                this.load().then(() => {
                    // 更新列表之后，原来的选择可能已不存在，这里暂存然后重新查找一遍
                    MComplex.watch.itemVMs.handler.call(this, this.itemVMs);
                });
            }
        }
        this.$on('virtual-scroll', () => {
            this.ensureSelectedInItemVMs();
        });
    },
    methods: {
        isPrimitive(value) {
            return typeof value !== 'object' || value === null;
        },
        isSimpleArray(arr) {
            if (!Array.isArray(arr)) {
                return false; // 如果不是数组类型，则不满足条件，直接返回 false
            }
            return arr.every((item) =>
                typeof item !== 'object', // 使用 typeof 判断是否为简单数据类型
            );
        },
        getRealItem(item, field) {
            return this.isSimpleArray(field) ? item.simple : item;
        },
        handleData() {
            // @TODO: undefined or null
            this.currentDataSource = this.normalizeDataSource(this.dataSource || this.data);
            if (this.currentDataSource && this.initialLoad) {
                if (this.$env.VUE_APP_DESIGNER)
                    return;
                this.load().then(() => {
                    // 更新列表之后，原来的选择可能已不存在，这里暂存然后重新查找一遍
                    MComplex.watch.itemVMs.handler.call(this, this.itemVMs);
                });
            }
        },
        getExtraParams() {
            return { filterText: this.filterText };
        },
        getDataSourceOptions() {
            return {
                viewMode: this.pageable === 'load-more' || this.pageable === 'auto-more' ? 'more' : 'page',
                paging: this.paging,
                remotePaging: this.remotePaging,
                filtering: this.filtering,
                remoteFiltering: this.remoteFiltering,
                getExtraParams: this.getExtraParams,
            };
        },
        normalizeDataSource(dataSource) {
            const options = this.getDataSourceOptions();
            if (dataSource instanceof DataSource)
                return dataSource;
            else if (dataSource instanceof Array) {
                options.data = Array.from(dataSource);
                return new DataSource(options);
            } else if (dataSource instanceof Function) {
                const self = this;
                options.load = function load(params) {
                    self.$emitSyncParams(params);
                    const result = dataSource(params);
                    if (result instanceof Promise)
                        return result.catch(
                            () => (this.currentLoading = false),
                        );
                    else if (result instanceof Array)
                        return Promise.resolve(result);
                    else
                        return Promise.resolve(result);
                };
                return new DataSource(options);
            } else if (dataSource instanceof Object) {
                if (dataSource.hasOwnProperty('list') && Array.isArray(dataSource.list))
                    return new DataSource(Object.assign(options, dataSource, {
                        data: dataSource.list,
                    }));
                return new DataSource(Object.assign(options, dataSource));
            } else
                return undefined;
        },
        shift(count) {
            if (this.readonly || this.disabled)
                return;
            let selectedIndex = this.itemVMs.indexOf(this.selectedVM);
            if (count > 0) {
                for (
                    let i = selectedIndex + count;
                    i < this.itemVMs.length;
                    i++
                ) {
                    const itemVM = this.itemVMs[i];
                    if (!itemVM.disabled) {
                        this.selectedVM = itemVM;
                        this.$emit(
                            'shift',
                            {
                                selectedIndex,
                                selectedVM: itemVM,
                                value: this.$at(itemVM.item, this.valueField),
                            },
                            this,
                        );
                        this.ensureFocusedInView();
                        break;
                    }
                }
            } else if (count < 0) {
                if (selectedIndex === -1)
                    selectedIndex = this.itemVMs.length;
                for (let i = selectedIndex + count; i >= 0; i--) {
                    const itemVM = this.itemVMs[i];
                    if (!itemVM.disabled) {
                        this.selectedVM = itemVM;
                        this.$emit(
                            'shift',
                            {
                                selectedIndex,
                                selectedVM: itemVM,
                                value: this.$at(itemVM.item, this.valueField),
                            },
                            this,
                        );
                        this.ensureFocusedInView();
                        break;
                    }
                }
            }
        },
        ensureFocusedInView(natural) {
            const focusedVM = this.focusedVM || this.selectedVM;
            if (!focusedVM)
                return;
            const focusedEl = focusedVM.$el;
            if (!focusedEl)
                return;
            let parentEl = focusedEl.parentElement;
            parentEl = findScrollParent(focusedEl); // focusedEl.parentElement不一定可以滚动，需要找到滚动的父元素
            if (!parentEl)
                return;
            const selectedIndex = this.itemVMs.indexOf(focusedVM);
            if (parentEl.scrollTop < focusedEl.offsetTop + focusedEl.offsetHeight - parentEl.clientHeight) {
                if (natural)
                    parentEl.scrollTop
                        = focusedEl.offsetTop - focusedEl.offsetHeight;
                else
                    parentEl.scrollTop
                        = focusedEl.offsetTop
                            + focusedEl.offsetHeight
                            - parentEl.clientHeight;
                if (selectedIndex === this.itemVMs.length - 1) {
                    (this.pageable === 'auto-more'
                        || (this.$options.isSelect && this.pageable))
                        && this.debouncedLoad(true); // 保证显示加载中，但又不是全部数据
                    this.$nextTick(
                        () =>
                            (parentEl.scrollTop
                                = parentEl.scrollHeight - parentEl.clientHeight),
                    );
                }
            }
            if (parentEl.scrollTop > focusedEl.offsetTop)
                parentEl.scrollTop = focusedEl.offsetTop;
        },
        /**
         * 保证选择项是 itemVMs 中的。由于切换分页、虚拟列表等会将内部组件节点干掉重新渲染，于是需要这个函数。
         */
        ensureSelectedInItemVMs() {
            if (this.multiple) {
                for (let i = 0; i < this.selectedVMs.length; i++) {
                    const oldVM = this.selectedVMs[i];
                    if (!this.itemVMs.includes(oldVM)) {
                        const selectedVM = this.itemVMs.find((itemVM) => this.$at(oldVM.item, this.valueField) && this.$at(oldVM.item, this.valueField) === this.$at(itemVM.item, this.valueField));
                        if (selectedVM) {
                            this.selectedVMs[i] = selectedVM;
                            selectedVM.currentSelected = true;
                        }
                    }
                }
            } else {
                if (this.selectedVM && !this.itemVMs.includes(this.selectedVM)) {
                    const selectedVM = this.itemVMs.find((itemVM) => this.$at(this.selectedVM.item, this.valueField) && this.$at(this.selectedVM.item, this.valueField) === this.$at(itemVM.item, this.valueField));
                    if (selectedVM)
                        this.selectedVM = selectedVM;
                }
            } // MComplex.watch.itemVMs.handler.call(this, this.itemVMs);
        },
        fastLoad(more, keep) {
            this.currentDataSource.filter(this.filtering);
            return this.currentDataSource.mustRemote() ? this.debouncedLoad(more, keep) : this.load(more, keep);
        },
        load(more) {
            const dataSource = this.currentDataSource;
            if (!dataSource)
                return;
            if (this.currentLoading)
                return Promise.resolve();
            if (this.$emitPrevent('before-load', undefined, this))
                return;
            this.currentLoading = true;
            return dataSource[more ? 'loadMore' : 'load']()
                .then((data) => {
                    this.currentLoading = false;
                    if (
                        this.pageable === true
                        || this.pageable === 'pagination'
                    ) {
                        if (
                            this.currentDataSource.paging
                            && this.currentDataSource.paging.number
                                > this.currentDataSource.totalPage
                        )
                            this.page(1); // 数据发生变更时，回归到第 1 页
                    }
                    this.ensureSelectedInItemVMs();
                    this.$emit('load', undefined, this);
                    return data;
                })
                .catch(() => (this.currentLoading = false));
        },
        async reload() {
            // readme: 目前使用场景中存在着用户通过props间接改变组件内部状态后同步调用reload的情况，在这里等待组件内部状态更新完成。
            await new Promise((res) => { this.$nextTick(() => res()); });
            // fix: 2891721709081088 数据源为变量时，调用reload会清空数据
            if (!this.currentDataSource._load || typeof this.currentDataSource._load !== 'function')
                return;
            this.currentDataSource.clearLocalData();
            const {
                paging: oldPaging,
            } = this.currentDataSource;
            let paging;
            if (oldPaging) {
                const { size, number } = oldPaging;
                paging = {
                    size,
                    oldSize: size,
                    number: 1,
                    oldNumber: number,
                };
            }
            try {
                this.currentDataSource.filter(this.filtering);
                this.currentDataSource.page(paging);
                this.load();
                this.$emit('page', paging, this);
                this.$emit('update:page-number', 1, this);
            } catch (error) {
                // eslint-disable-next-line no-console
                console.log(error);
            }
        },
        page(number, size) {
            if (size === undefined)
                size = this.currentDataSource && this.currentDataSource.paging && this.currentDataSource.paging.size;
            const paging = {
                size,
                oldSize: this.currentDataSource && this.currentDataSource.paging && this.currentDataSource.paging.size,
                number,
                oldNumber: this.currentDataSource && this.currentDataSource.paging && this.currentDataSource.paging.number,
            };
            if (this.$emitPrevent('before-page', paging, this))
                return;
            this.currentDataSource.page(paging);
            this.load();
            this.$emit('page', paging, this);
            this.$emit('update:page-number', number, this);
        },
        onScroll(e) {
            this.hasScroll = true;
            this.throttledVirtualScroll(e);
            if (!(this.pageable === 'auto-more' || (this.pageable === true && this.$options.isSelect)))
                return;
            if (this.currentLoading)
                return;
            const el = e.target;
            if (Math.abs(el.scrollHeight - (el.scrollTop + el.clientHeight)) <= 1 && this.currentDataSource && this.currentDataSource.hasMore())
                this.debouncedLoad(true);
        },
        onInput(value) {
            if (this.$emitPrevent('before-filter', { filterText: value }, this))
                return;
            this.filterText = value;
            this.fastLoad(false, true);
        },
        checkAll(checked) {
            // Check if enabled
            if (this.readonly || this.disabled)
                return;
            const selectedVMs = [...this.selectedVMs];
            const oldValue = selectedVMs.map((itemVM) => itemVM.value);
            this.itemVMs.forEach((itemVM) => {
                if (itemVM.disabled)
                    return;
                if (checked && !selectedVMs.includes(itemVM)) {
                    itemVM.currentSelected = true;
                    selectedVMs.push(itemVM);
                    itemVM.$emit('update:selected', itemVM.currentSelected);
                } else if (!checked && selectedVMs.includes(itemVM)) {
                    itemVM.currentSelected = false;
                    selectedVMs.splice(selectedVMs.indexOf(itemVM), 1);
                    itemVM.$emit('update:selected', itemVM.currentSelected);
                }
            });
            let value = selectedVMs.map((itemVM) => itemVM.value);
            if (this.converter) {
                value = this.currentConverter.get(value);
            }

            this.selectedVMs = selectedVMs;
            this.$emit('input', value, this);
            this.$emit('update:value', value, this);
            this.$emit('checkAll', { value, oldValue, checked }, this);
        },
        onChangePageSize(event) {
            this.currentPageSize = event.pageSize;
            const currentDataSource = this.currentDataSource;
            this.page(currentDataSource.paging ? currentDataSource.paging.number : this.pageNumber, event.pageSize);
        },
        loadTo(page) {
            const dataSource = this.currentDataSource;
            if (!(dataSource && dataSource.paging))
                return;
            if(dataSource._load && typeof dataSource._load === 'function') {
                dataSource.clearLocalData();
            }
            let currentPage = page;
            if(['', null, undefined].includes(page)) {
                currentPage = dataSource.paging.number;
            }
            if(currentPage === dataSource.paging.number) {
                this.load(false, { number: currentPage });
            } else {
                dataSource.paging.number = page;
            }
        },
    },
};
</script>

<style module src="./index.css"></style>
