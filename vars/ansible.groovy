/**
 * Run a single ansible playbook.
 */
@NonCPS
def call(playbook, params) {
    node { // we allocate nodes, since the playbooks are quite heavy.
        def playbookArgs = params.collect({ k, v -> "-e '$k=$v'" }).join(' ');
        sh """
        echo ansible-playbook ${playbook} ${playbookArgs}
        """
    }
}


