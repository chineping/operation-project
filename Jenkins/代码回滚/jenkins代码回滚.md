#jenkins代码回滚说明文档

##jenkins的JOB配置

详细见本目录下的回滚配置1.png和回滚配置2.png

1.png为选项参数（hosts）和字符参数的配置（rollback_datetime）

2.png为构建执行shell
	
	cd /etc/ansible/rollback/
	bash jxrollback_choice.sh $hosts $rollback_datetime > rollback_choice.out 2>&1 &
	##将1中的两个参数传入到jxrollback_choice.sh脚本呢中
	sleep 10
	cat /etc/ansible/rollback/rollback_choice.out

##jxrollback_choice.sh脚本


cd  /etc/ansible/rollback/

	#!/bin/sh
	
	while [ -n "$1" ]
	do
	    case "$1" in
	        jx-admin-[1-2]) ansible-playbook /etc/ansible/rollback/jx/rollback_jx_admin.yaml --extra-vars "hosts=$1 rollback_datetime=$2" ;break ;;
	        jx_backend01) ansible-playbook /etc/ansible/rollback/jx/rollback_jx_adminh5.yaml --extra-vars "hosts=$1 rollback_datetime=$2" ;break ;;
	        jx-datainner-[1-2]) ansible-playbook /etc/ansible/rollback/jx/rollback_jx_datainner.yaml --extra-vars "hosts=$1 rollback_datetime=$2" ;break ;;
	        jx-eureka-[1-3]) ansible-playbook /etc/ansible/rollback/jx/rollback_jx_eureka.yaml --extra-vars "hosts=$1 rollback_datetime=$2" ;break ;;
	        jx-frontmanager-[1-2]) ansible-playbook /etc/ansible/rollback/jx/rollback_jx_frontmanager.yaml --extra-vars "hosts=$1 rollback_datetime=$2" ;break ;;
	        jx_frt0[1-2]) ansible-playbook /etc/ansible/rollback/jx/rollback_jx_producth5.yaml --extra-vars "hosts=$1 rollback_datetime=$2" ;break ;;
	        jx-gateway) ansible-playbook /etc/ansible/rollback/jx/rollback_jx_gateway.yaml --extra-vars "hosts=$1 rollback_datetime=$2" ;break ;;
	        jx-auth) ansible-playbook /etc/ansible/rollback/jx/rollback_jx_auth.yaml --extra-vars "hosts=$1 rollback_datetime=$2" ;break ;;
	        jx-product-[1-2]) ansible-playbook /etc/ansible/rollback/jx/rollback_jx_product.yaml --extra-vars "hosts=$1 rollback_datetime=$2" ;break ;;
	        jx-schedule) ansible-playbook /etc/ansible/rollback/jx/rollback_jx_schedule.yaml --extra-vars "hosts=$1 rollback_datetime=$2" ;break ;;
	        q-sj-frtserver[1-2]) ansible-playbook /etc/ansible/rollback/sj/rollback_sj_product.yaml --extra-vars "hosts=$1 rollback_datetime=$2" ;break ;;
	        sj_frt0[1-2]) ansible-playbook /etc/ansible/rollback/sj/rollback_sj_producth5.yaml --extra-vars "hosts=$1 rollback_datetime=$2" ;break ;;
	        q-xk-frtserver-externalservice0[1-2]) ansible-playbook /etc/ansible/rollback/xk/rollback_xk_externalservice.yaml --extra-vars "hosts=$1 rollback_datetime=$2" ;break ;;
	        q-xk-frtserver-frontservice0[1-2]) ansible-playbook /etc/ansible/rollback/xk/rollback_xk_frontservice.yaml --extra-vars "hosts=$1 rollback_datetime=$2" ;break ;;
	        xk_frt0[1-2]) ansible-playbook /etc/ansible/rollback/xk/rollback_xk_h5.yaml --extra-vars "hosts=$1 rollback_datetime=$2" ;break ;;
	        yunweiji) ansible-playbook /etc/ansible/rollback/test/rollback_test_h5.yaml --extra-vars "hosts=$1 rollback_datetime=$2" ;break ;;
	    esac
	done


根据hosts选择对应的项，执行相应的yaml脚本，同时将选项参数（hosts）和字符参数（rollback_datetime）传过去。


##yaml脚本

###1、jar包类脚本

	---
	- hosts: "{{ hosts }}"
	  vars:
	    pkg_pro: /opt/trainingcamp/trainingcamp-frontservice.jar
	    dir_bak: /opt/trainingcamp/jar_bak
	  tasks:
	    - name: remove useless package
	      file:
	        path: "{{ pkg_pro }}"
	        state: absent
	
	    - name: copy rollback time package
	      copy: src={{ dir_bak }}/{{ rollback_datetime }}+trainingcamp-frontservice.jar dest="{{ pkg_pro }}" remote_src=yes
	
	    - name: restart remote host service
      shell: /opt/trainingcamp/ms {{ pkg_pro }} pro


###2、H5前端类脚本

	---
	- hosts: "{{ hosts }}"
	  vars:
	    pkg_pro: /opt/trainingcamp
	    dir_bak: /opt/h5_bak
	  tasks:
	    - name: remove useless package
	      file:
	        path: "{{ pkg_pro }}"
	        state: absent
	
	    - name: copy rollback time package
	      copy: src={{ dir_bak }}/{{ rollback_datetime }}+trainingcamp/ dest={{ pkg_pro }}
      remote_src: yes